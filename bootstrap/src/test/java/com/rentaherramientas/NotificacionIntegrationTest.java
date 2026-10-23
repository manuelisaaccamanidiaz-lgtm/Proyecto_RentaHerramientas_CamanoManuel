package com.rentaherramientas;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
 * Notificaciones de punta a punta contra PostgreSQL real: el cliente crea una
 * reserva → tras el commit, el listener AFTER_COMMIT avisa al proveedor dueño →
 * solo él puede marcarla como leída. El listener es síncrono, así que al volver
 * el POST la notificación ya está guardada.
 */
class NotificacionIntegrationTest extends IntegracionBase {

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
                {"nombre": "Pulidora de prueba", "descripcion": "Herramienta de la prueba",
                 "tarifaDiaria": 30000, "categoriaIds": [%d]}
                """.formatted(categoriaId)))
                .andExpect(status().isCreated()));

        String emailCliente = emailUnico("cliente");
        registrarCliente(emailCliente).andExpect(status().isCreated());
        tokenCliente = login(emailCliente, CLAVE);
    }

    private ResultActions conteo(String token) throws Exception {
        return mvc.perform(conToken(get("/api/notificaciones/mias/conteo"), token)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("crear reserva → 1 no leída del proveedor → otro usuario no puede marcarla → el dueño sí → 0")
    void reservaNotificaAlProveedor() throws Exception {
        conteo(tokenProveedor).andExpect(jsonPath("$.noLeidas").value(0));

        mvc.perform(conJson(post("/api/reservas"), tokenCliente, """
                {"herramientaId": %d, "fechaInicio": "%s", "fechaFin": "%s"}
                """.formatted(herramientaId, HOY.plusDays(3), HOY.plusDays(4))))
                .andExpect(status().isCreated());

        conteo(tokenProveedor).andExpect(jsonPath("$.noLeidas").value(1));
        ResultActions listado = mvc.perform(conToken(get("/api/notificaciones/mias")
                        .param("soloNoLeidas", "true"), tokenProveedor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.contenido[0].mensaje")
                        .value("Nueva solicitud de reserva para Pulidora de prueba"))
                .andExpect(jsonPath("$.contenido[0].leida").value(false));
        long notificacionId = ((Number) leer(listado, "$.contenido[0].id")).longValue();

        // El cliente no recibió nada y no puede tocar la notificación del proveedor: 404, como si no existiera.
        conteo(tokenCliente).andExpect(jsonPath("$.noLeidas").value(0));
        mvc.perform(conToken(patch("/api/notificaciones/{id}/leida", notificacionId), tokenCliente))
                .andExpect(status().isNotFound());
        conteo(tokenProveedor).andExpect(jsonPath("$.noLeidas").value(1));

        mvc.perform(conToken(patch("/api/notificaciones/{id}/leida", notificacionId), tokenProveedor))
                .andExpect(status().isNoContent());
        conteo(tokenProveedor).andExpect(jsonPath("$.noLeidas").value(0));
    }

    @Test
    @DisplayName("aceptar avisa al cliente; marcar todas como leídas deja el conteo en 0")
    void aceptarNotificaAlClienteYMarcarTodas() throws Exception {
        long reservaId = leerId(mvc.perform(conJson(post("/api/reservas"), tokenCliente, """
                {"herramientaId": %d, "fechaInicio": "%s", "fechaFin": "%s"}
                """.formatted(herramientaId, HOY.plusDays(6), HOY.plusDays(7))))
                .andExpect(status().isCreated()));
        mvc.perform(conToken(patch("/api/reservas/{id}/aceptar", reservaId), tokenProveedor))
                .andExpect(status().isOk());

        conteo(tokenCliente).andExpect(jsonPath("$.noLeidas").value(1));
        mvc.perform(conToken(get("/api/notificaciones/mias"), tokenCliente))
                .andExpect(jsonPath("$.contenido[0].mensaje")
                        .value("Tu reserva #" + reservaId + " de Pulidora de prueba fue aceptada. Ya puedes pagarla."));

        mvc.perform(conToken(patch("/api/notificaciones/mias/leidas"), tokenCliente))
                .andExpect(status().isNoContent());
        conteo(tokenCliente).andExpect(jsonPath("$.noLeidas").value(0));
    }

    @Test
    @DisplayName("pago con TARJETA avisa al cliente con el número de factura y al proveedor")
    void pagoAprobadoNotificaAAmbos() throws Exception {
        long reservaId = leerId(mvc.perform(conJson(post("/api/reservas"), tokenCliente, """
                {"herramientaId": %d, "fechaInicio": "%s", "fechaFin": "%s"}
                """.formatted(herramientaId, HOY.plusDays(15), HOY.plusDays(16))))
                .andExpect(status().isCreated()));
        mvc.perform(conToken(patch("/api/reservas/{id}/aceptar", reservaId), tokenProveedor))
                .andExpect(status().isOk());
        String numeroFactura = leer(mvc.perform(conJson(post("/api/pagos"), tokenCliente, """
                {"reservaId": %d, "metodo": "TARJETA"}
                """.formatted(reservaId)))
                .andExpect(status().isCreated()), "$.factura.numero");

        mvc.perform(conToken(get("/api/notificaciones/mias"), tokenCliente))
                .andExpect(jsonPath("$.contenido[0].mensaje")
                        .value("Pago aprobado, tu factura N.º " + numeroFactura + " está lista."));
        mvc.perform(conToken(get("/api/notificaciones/mias"), tokenProveedor))
                .andExpect(jsonPath("$.contenido[0].mensaje")
                        .value("Se aprobó el pago de la reserva #" + reservaId + " de Pulidora de prueba."));
        // Proveedor: solicitud + pago; cliente: aceptación + pago.
        conteo(tokenProveedor).andExpect(jsonPath("$.noLeidas").value(2));
        conteo(tokenCliente).andExpect(jsonPath("$.noLeidas").value(2));
    }

    @Test
    @DisplayName("una reserva rechazada (409) no deja notificación")
    void sinCommitNoHayNotificacion() throws Exception {
        mvc.perform(conJson(post("/api/reservas"), tokenCliente, """
                {"herramientaId": %d, "fechaInicio": "%s", "fechaFin": "%s"}
                """.formatted(herramientaId, HOY.plusDays(10), HOY.plusDays(11))))
                .andExpect(status().isCreated());
        conteo(tokenProveedor).andExpect(jsonPath("$.noLeidas").value(1));

        mvc.perform(conJson(post("/api/reservas"), tokenCliente, """
                {"herramientaId": %d, "fechaInicio": "%s", "fechaFin": "%s"}
                """.formatted(herramientaId, HOY.plusDays(11), HOY.plusDays(12))))
                .andExpect(status().isConflict());
        conteo(tokenProveedor).andExpect(jsonPath("$.noLeidas").value(1));
    }
}
