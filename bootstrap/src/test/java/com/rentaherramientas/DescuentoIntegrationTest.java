package com.rentaherramientas;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Descuento por herramienta contra PostgreSQL real: una reserva nueva paga la
 * tarifa rebajada y su factura cuadra con el pago; una reserva anterior al
 * descuento conserva su precio (precio_total es una foto del momento de reservar).
 */
class DescuentoIntegrationTest extends IntegracionBase {

    private static final LocalDate HOY = LocalDate.now(ZoneId.of("America/Bogota"));

    private String tokenProveedor;
    private String tokenCliente;
    private long categoriaId;
    private long herramientaId;

    @BeforeEach
    void prepararDatos() throws Exception {
        String tokenAdmin = login("admin@pruebas.test", "Admin123*");
        categoriaId = leerId(mvc.perform(conJson(post("/api/categorias"), tokenAdmin, """
                {"nombre": "Categoría %s", "descripcion": "Creada por la prueba"}
                """.formatted(System.nanoTime())))
                .andExpect(status().isCreated()));

        String emailProveedor = emailUnico("proveedor");
        registrarProveedor(emailProveedor).andExpect(status().isCreated());
        tokenProveedor = login(emailProveedor, CLAVE);
        herramientaId = leerId(mvc.perform(conJson(post("/api/herramientas"), tokenProveedor, """
                {"nombre": "Pulidora de prueba", "tarifaDiaria": 45000, "categoriaIds": [%d]}
                """.formatted(categoriaId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descuentoPorcentaje").doesNotExist())
                .andExpect(jsonPath("$.tarifaConDescuento").value(45000)));

        String emailCliente = emailUnico("cliente");
        registrarCliente(emailCliente).andExpect(status().isCreated());
        tokenCliente = login(emailCliente, CLAVE);
    }

    private ResultActions ponerDescuento(Integer porcentaje) throws Exception {
        return mvc.perform(conJson(put("/api/herramientas/{id}", herramientaId), tokenProveedor, """
                {"nombre": "Pulidora de prueba", "tarifaDiaria": 45000, "descuentoPorcentaje": %s,
                 "categoriaIds": [%d]}
                """.formatted(porcentaje, categoriaId)));
    }

    private ResultActions crearReserva(LocalDate inicio, LocalDate fin) throws Exception {
        return mvc.perform(conJson(post("/api/reservas"), tokenCliente, """
                {"herramientaId": %d, "fechaInicio": "%s", "fechaFin": "%s"}
                """.formatted(herramientaId, inicio, fin)));
    }

    private ResultActions aceptarYPagar(long reservaId) throws Exception {
        mvc.perform(conToken(patch("/api/reservas/{id}/aceptar", reservaId), tokenProveedor))
                .andExpect(status().isOk());
        return mvc.perform(conJson(post("/api/pagos"), tokenCliente, """
                {"reservaId": %d, "metodo": "TARJETA"}
                """.formatted(reservaId)));
    }

    @Test
    @DisplayName("10 % sobre 45.000 → reserva de 2 días = 81.000 → pago con TARJETA y factura de 81.000")
    void reservaConDescuentoSeFacturaCuadrada() throws Exception {
        ponerDescuento(10)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tarifaDiaria").value(45000))
                .andExpect(jsonPath("$.descuentoPorcentaje").value(10))
                .andExpect(jsonPath("$.tarifaConDescuento").value(40500));

        long reservaId = leerId(crearReserva(HOY.plusDays(5), HOY.plusDays(6))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dias").value(2))
                .andExpect(jsonPath("$.precioTotal").value(81000)));

        // Si la factura no cuadrara con el pago, EmisorFactura lanzaría FacturaInvalidaException.
        aceptarYPagar(reservaId)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("APROBADO"))
                .andExpect(jsonPath("$.monto").value(81000))
                .andExpect(jsonPath("$.factura.numero").isNotEmpty())
                .andExpect(jsonPath("$.factura.total").value(81000));
    }

    @Test
    @DisplayName("Una reserva creada antes del descuento conserva su precio original")
    void reservaAnteriorConservaPrecio() throws Exception {
        long anterior = leerId(crearReserva(HOY.plusDays(10), HOY.plusDays(12))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.precioTotal").value(135000)));

        ponerDescuento(10).andExpect(status().isOk());

        mvc.perform(conToken(get("/api/reservas/{id}", anterior), tokenCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precioTotal").value(135000));
        aceptarYPagar(anterior)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.monto").value(135000))
                .andExpect(jsonPath("$.factura.total").value(135000));

        // Y una nueva, en otras fechas, ya usa la tarifa rebajada (3 × 40.500).
        crearReserva(HOY.plusDays(20), HOY.plusDays(22))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.precioTotal").value(121500));
    }

    @Test
    @DisplayName("Un descuento fuera de 1..90 responde 400")
    void descuentoFueraDeRango() throws Exception {
        ponerDescuento(91).andExpect(status().isBadRequest());
        ponerDescuento(0).andExpect(status().isBadRequest());
    }
}
