package com.rentaherramientas;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Cambio de contraseña propia de punta a punta: después del cambio el login
 * con la contraseña vieja responde 401 y con la nueva emite un token. Los
 * errores de contraseña (actual incorrecta, nueva igual) responden 400, no 401,
 * para que el frontend no cierre la sesión.
 */
class CambiarPasswordIntegrationTest extends IntegracionBase {

    private static final String NUEVA = "NuevaClave456*";

    @Test
    @DisplayName("Registro → login → cambio (204) → la contraseña vieja falla y la nueva funciona")
    void cambioCompleto() throws Exception {
        String email = emailUnico("cambia-clave");
        registrarCliente(email).andExpect(status().isCreated());
        String token = login(email, CLAVE);

        mvc.perform(conJson(patch("/api/usuarios/me/password"), token, """
                        {"passwordActual": "%s", "passwordNueva": "%s"}
                        """.formatted(CLAVE, NUEVA)))
                .andExpect(status().isNoContent());

        mvc.perform(conJson(post("/api/auth/login"), null, """
                        {"email": "%s", "password": "%s"}
                        """.formatted(email, CLAVE)))
                .andExpect(status().isUnauthorized());
        login(email, NUEVA);
    }

    @Test
    @DisplayName("Actual incorrecta → 400; nueva igual a la actual → 400; nueva corta → 400 por campo")
    void erroresDeValidacion() throws Exception {
        String email = emailUnico("clave-invalida");
        registrarCliente(email).andExpect(status().isCreated());
        String token = login(email, CLAVE);

        mvc.perform(conJson(patch("/api/usuarios/me/password"), token, """
                        {"passwordActual": "NoEsLaClave*", "passwordNueva": "%s"}
                        """.formatted(NUEVA)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("La contraseña actual no es correcta"));

        mvc.perform(conJson(patch("/api/usuarios/me/password"), token, """
                        {"passwordActual": "%s", "passwordNueva": "%s"}
                        """.formatted(CLAVE, CLAVE)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("La contraseña nueva debe ser distinta de la actual"));

        mvc.perform(conJson(patch("/api/usuarios/me/password"), token, """
                        {"passwordActual": "%s", "passwordNueva": "corta"}
                        """.formatted(CLAVE)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.passwordNueva").exists());

        login(email, CLAVE);
    }
}
