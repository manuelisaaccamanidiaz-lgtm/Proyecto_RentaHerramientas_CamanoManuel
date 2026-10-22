package com.rentaherramientas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Cambio de rol de punta a punta: el ADMIN convierte un CLIENTE en PROVEEDOR,
 * la fila de "clientes" se borra y se crea la de "proveedores", y el nuevo rol
 * solo se aplica con un token emitido después del cambio (el viejo lleva el rol
 * anterior en el claim "rol").
 */
class CambiarRolIntegrationTest extends IntegracionBase {

    private static final String ADMIN_EMAIL = "admin@pruebas.test";
    private static final String ADMIN_CLAVE = "Admin123*";

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    @DisplayName("CLIENTE → PROVEEDOR: cambia las filas de subtipo y solo el token nuevo entra a /api/proveedor/**")
    void clienteAProveedor() throws Exception {
        String email = emailUnico("cliente-a-proveedor");
        long usuarioId = leerId(registrarCliente(email).andExpect(status().isCreated()));
        String tokenViejo = login(email, CLAVE);
        String tokenAdmin = login(ADMIN_EMAIL, ADMIN_CLAVE);
        String nit = ThreadLocalRandom.current().nextLong(100_000_000L, 1_000_000_000L) + "-1";

        mvc.perform(conJson(patch("/api/admin/usuarios/{id}/rol", usuarioId), tokenAdmin, """
                        {"rol": "PROVEEDOR",
                         "datosProveedor": {"razonSocial": "Herramientas Convertidas SAS", "nit": "%s"}}
                        """.formatted(nit)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("PROVEEDOR"))
                .andExpect(jsonPath("$.datosProveedor.nit").value(nit))
                .andExpect(jsonPath("$.datosCliente").isEmpty());

        assertEquals(0, contarFilas("clientes", usuarioId), "se borró la fila del subtipo anterior");
        assertEquals(1, contarFilas("proveedores", usuarioId), "se creó la fila del nuevo subtipo");

        String tokenNuevo = login(email, CLAVE);
        mvc.perform(conToken(get("/api/proveedor/herramientas"), tokenNuevo))
                .andExpect(status().isOk());
        mvc.perform(conToken(get("/api/proveedor/herramientas"), tokenViejo))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Mismo rol → 409; el ADMIN cambiando su propio rol → 403")
    void mismoRolYPropioRol() throws Exception {
        String email = emailUnico("cliente-mismo-rol");
        long usuarioId = leerId(registrarCliente(email).andExpect(status().isCreated()));
        String tokenAdmin = login(ADMIN_EMAIL, ADMIN_CLAVE);

        mvc.perform(conJson(patch("/api/admin/usuarios/{id}/rol", usuarioId), tokenAdmin, """
                        {"rol": "CLIENTE",
                         "datosCliente": {"documento": "123456789", "telefono": "3001234567", "direccion": "Calle 1"}}
                        """))
                .andExpect(status().isConflict());

        long adminId = leerId(mvc.perform(conToken(get("/api/usuarios/me"), tokenAdmin)));
        mvc.perform(conJson(patch("/api/admin/usuarios/{id}/rol", adminId), tokenAdmin, """
                        {"rol": "PROVEEDOR",
                         "datosProveedor": {"razonSocial": "Admin SAS", "nit": "900000001-1"}}
                        """))
                .andExpect(status().isForbidden());
    }

    private long contarFilas(String tabla, long usuarioId) {
        return jdbcClient.sql("select count(*) from " + tabla + " where usuario_id = :id")
                .param("id", usuarioId).query(Long.class).single();
    }
}
