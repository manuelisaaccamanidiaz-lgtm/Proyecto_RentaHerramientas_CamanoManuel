package com.rentaherramientas;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Base de las pruebas de integración: levanta la aplicación completa (perfil
 * "test") contra PostgreSQL en Testcontainers y ofrece atajos para registrar
 * usuarios e iniciar sesión por la API REST, igual que lo haría un cliente real.
 * Cada usuario lleva un email único para que las pruebas no dependan del orden.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(ContenedorPostgres.class)
public abstract class IntegracionBase {

    protected static final String CLAVE = "Clave123*";

    @Autowired
    protected MockMvc mvc;

    /** Email irrepetible con el prefijo dado (p. ej. "cliente-3f2a...@pruebas.test"). */
    protected static String emailUnico(String prefijo) {
        return prefijo + "-" + UUID.randomUUID().toString().substring(0, 8) + "@pruebas.test";
    }

    /** Petición con cuerpo JSON y, si se indica, el token Bearer. */
    protected static MockHttpServletRequestBuilder conJson(MockHttpServletRequestBuilder peticion, String token,
                                                           String json) {
        if (token != null) {
            peticion.header("Authorization", "Bearer " + token);
        }
        return peticion.contentType(MediaType.APPLICATION_JSON).content(json);
    }

    protected static MockHttpServletRequestBuilder conToken(MockHttpServletRequestBuilder peticion, String token) {
        return peticion.header("Authorization", "Bearer " + token);
    }

    /** Lee un valor del JSON de la respuesta con una expresión JsonPath ("$.id"). */
    /** 9 dígitos al azar: documento del cliente y NIT del proveedor son únicos en la base. */
    private static long numeroAleatorio() {
        return ThreadLocalRandom.current().nextLong(100_000_000L, 1_000_000_000L);
    }

    protected static <T> T leer(ResultActions resultado, String ruta) throws Exception {
        return JsonPath.read(resultado.andReturn().getResponse().getContentAsString(), ruta);
    }

    protected static long leerId(ResultActions resultado) throws Exception {
        return ((Number) leer(resultado, "$.id")).longValue();
    }

    protected ResultActions registrarCliente(String email) throws Exception {
        return mvc.perform(conJson(post("/api/auth/registro"), null, """
                {"nombre": "Cliente de Prueba", "email": "%s", "password": "%s", "rol": "CLIENTE",
                 "datosCliente": {"documento": "%d", "telefono": "3001234567",
                                  "direccion": "Calle 36 # 27-50, Bucaramanga"}}
                """.formatted(email, CLAVE, numeroAleatorio())));
    }

    protected ResultActions registrarProveedor(String email) throws Exception {
        return mvc.perform(conJson(post("/api/auth/registro"), null, """
                {"nombre": "Proveedor de Prueba", "email": "%s", "password": "%s", "rol": "PROVEEDOR",
                 "datosProveedor": {"razonSocial": "Ferretería de Pruebas SAS", "nit": "%d-7"}}
                """.formatted(email, CLAVE, numeroAleatorio())));
    }

    /** Inicia sesión y devuelve el JWT; falla la prueba si el login no responde 200. */
    protected String login(String email, String password) throws Exception {
        ResultActions resultado = mvc.perform(conJson(post("/api/auth/login"), null, """
                {"email": "%s", "password": "%s"}
                """.formatted(email, password)))
                .andExpect(status().isOk());
        return leer(resultado, "$.token");
    }
}
