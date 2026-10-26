package com.rentaherramientas;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Autenticación y autorización de punta a punta: registro, login con JWT,
 * acceso al perfil propio y las respuestas 401 (sin token) y 403 (rol sin permiso).
 */
class AuthIntegrationTest extends IntegracionBase {

    @Test
    @DisplayName("registro → login → GET /api/usuarios/me responde 200 con los datos del usuario")
    void registroLoginYPerfil() throws Exception {
        String email = emailUnico("cliente");

        registrarCliente(email)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.rol").value("CLIENTE"));

        String token = login(email, CLAVE);

        mvc.perform(conToken(get("/api/usuarios/me"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
    }

    @Test
    @DisplayName("sin token, un endpoint protegido responde 401")
    void sinTokenResponde401() throws Exception {
        mvc.perform(get("/api/usuarios/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("un CLIENTE en /api/admin/** recibe 403")
    void clienteEnAdminResponde403() throws Exception {
        String email = emailUnico("cliente");
        registrarCliente(email).andExpect(status().isCreated());
        String token = login(email, CLAVE);

        mvc.perform(conToken(get("/api/admin/usuarios"), token))
                .andExpect(status().isForbidden());
    }
}
