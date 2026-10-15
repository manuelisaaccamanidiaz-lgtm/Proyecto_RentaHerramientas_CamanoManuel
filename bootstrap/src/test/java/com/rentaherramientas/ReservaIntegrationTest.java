package com.rentaherramientas;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Ciclo de vida de una reserva contra PostgreSQL real: crear → aceptar → pagar →
 * entregar → devolver, y el rechazo (409) de una reserva que se cruza con otra.
 * Cada prueba crea su propio proveedor, herramienta y cliente.
 */
class ReservaIntegrationTest extends IntegracionBase {

    /** "Hoy" según el reloj de negocio de la app (Bogotá). */
    private static final LocalDate HOY = LocalDate.now(ZoneId.of("America/Bogota"));

    private String tokenProveedor;
    private String tokenCliente;
    private long herramientaId;

    @BeforeEach
    void prepararDatos() throws Exception {
        String tokenAdmin = login("admin@pruebas.test", "Admin123*");
        long categoriaId = leerId(mvc.perform(conJson(post("/api/categorias"), tokenAdmin, """
                {"nombre": "Categoría %s", "descripcion": "Creada por la prueba"}
                """.formatted(System.nanoTime())))
                .andExpect(status().isCreated()));

        String emailProveedor = emailUnico("proveedor");
        registrarProveedor(emailProveedor).andExpect(status().isCreated());
        tokenProveedor = login(emailProveedor, CLAVE);
        herramientaId = leerId(mvc.perform(conJson(post("/api/herramientas"), tokenProveedor, """
                {"nombre": "Taladro de prueba", "descripcion": "Herramienta de la prueba",
                 "tarifaDiaria": 25000, "categoriaIds": [%d]}
                """.formatted(categoriaId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("DISPONIBLE")));

        String emailCliente = emailUnico("cliente");
        registrarCliente(emailCliente).andExpect(status().isCreated());
        tokenCliente = login(emailCliente, CLAVE);
    }

    private ResultActions crearReserva(LocalDate inicio, LocalDate fin) throws Exception {
        return mvc.perform(conJson(post("/api/reservas"), tokenCliente, """
                {"herramientaId": %d, "fechaInicio": "%s", "fechaFin": "%s"}
                """.formatted(herramientaId, inicio, fin)));
    }

    private ResultActions accionProveedor(long reservaId, String accion) throws Exception {
        return mvc.perform(conToken(patch("/api/reservas/{id}/{accion}", reservaId, accion), tokenProveedor));
    }

    @Test
    @DisplayName("crear → aceptar → pagar → entregar → devolver")
    void flujoCompleto() throws Exception {
        long reservaId = leerId(crearReserva(HOY.plusDays(5), HOY.plusDays(7))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.dias").value(3))
                .andExpect(jsonPath("$.precioTotal").value(75000)));

        accionProveedor(reservaId, "aceptar")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACEPTADA"));

        mvc.perform(conJson(post("/api/pagos"), tokenCliente, """
                {"reservaId": %d, "metodo": "TARJETA"}
                """.formatted(reservaId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("APROBADO"))
                .andExpect(jsonPath("$.monto").value(75000))
                .andExpect(jsonPath("$.factura.numero").isNotEmpty());

        accionProveedor(reservaId, "entregar")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ENTREGADA"))
                .andExpect(jsonPath("$.pagada").value(true));

        accionProveedor(reservaId, "devolver")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DEVUELTA"))
                .andExpect(jsonPath("$.fechaDevolucion").isNotEmpty());
    }

    @Test
    @DisplayName("una reserva que se cruza con otra vigente de la misma herramienta responde 409")
    void solapamientoResponde409() throws Exception {
        crearReserva(HOY.plusDays(10), HOY.plusDays(12)).andExpect(status().isCreated());

        crearReserva(HOY.plusDays(12), HOY.plusDays(14))
                .andExpect(status().isConflict());
    }
}
