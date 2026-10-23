package com.rentaherramientas;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Favoritos contra PostgreSQL real: idempotencia de PUT/DELETE (la PK compuesta
 * y ON CONFLICT DO NOTHING), el listado paginado con los datos de la herramienta,
 * el 409 de una herramienta inactiva y el 403 para quien no es CLIENTE.
 */
class FavoritosIntegrationTest extends IntegracionBase {

    private String tokenProveedor;
    private String tokenCliente;
    private long taladroId;
    private long pulidoraId;

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
        taladroId = registrarHerramienta("Taladro de prueba", categoriaId);
        pulidoraId = registrarHerramienta("Pulidora de prueba", categoriaId);

        String emailCliente = emailUnico("cliente");
        registrarCliente(emailCliente).andExpect(status().isCreated());
        tokenCliente = login(emailCliente, CLAVE);
    }

    private long registrarHerramienta(String nombre, long categoriaId) throws Exception {
        return leerId(mvc.perform(conJson(post("/api/herramientas"), tokenProveedor, """
                {"nombre": "%s", "tarifaDiaria": 30000, "categoriaIds": [%d]}
                """.formatted(nombre, categoriaId)))
                .andExpect(status().isCreated()));
    }

    private ResultActions marcar(String token, long herramientaId) throws Exception {
        return mvc.perform(conToken(put("/api/favoritos/{id}", herramientaId), token));
    }

    private ResultActions misFavoritos() throws Exception {
        return mvc.perform(conToken(get("/api/favoritos/mios"), tokenCliente));
    }

    @Test
    @DisplayName("Marca 2 (una de ellas dos veces) → GET devuelve 2 → desmarca 1 (dos veces) → devuelve 1")
    void marcarListarYDesmarcar() throws Exception {
        marcar(tokenCliente, taladroId).andExpect(status().isNoContent());
        marcar(tokenCliente, pulidoraId).andExpect(status().isNoContent());
        marcar(tokenCliente, pulidoraId).andExpect(status().isNoContent()); // idempotente: no duplica

        misFavoritos()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(2))
                .andExpect(jsonPath("$.contenido.length()").value(2))
                // La más reciente primero, con los datos de la herramienta.
                .andExpect(jsonPath("$.contenido[0].id").value(pulidoraId))
                .andExpect(jsonPath("$.contenido[0].nombre").value("Pulidora de prueba"))
                .andExpect(jsonPath("$.contenido[0].tarifaDiaria").value(30000))
                .andExpect(jsonPath("$.contenido[1].id").value(taladroId));

        mvc.perform(conToken(delete("/api/favoritos/{id}", taladroId), tokenCliente))
                .andExpect(status().isNoContent());
        mvc.perform(conToken(delete("/api/favoritos/{id}", taladroId), tokenCliente))
                .andExpect(status().isNoContent()); // idempotente: ya no existía

        misFavoritos()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.contenido[0].id").value(pulidoraId));
    }

    @Test
    @DisplayName("Herramienta inactiva → 409; inexistente → 404")
    void herramientaInactivaOInexistente() throws Exception {
        mvc.perform(conToken(delete("/api/herramientas/{id}", taladroId), tokenProveedor))
                .andExpect(status().isNoContent());

        marcar(tokenCliente, taladroId).andExpect(status().isConflict());
        marcar(tokenCliente, Long.MAX_VALUE).andExpect(status().isNotFound());
        misFavoritos().andExpect(jsonPath("$.totalElementos").value(0));
    }

    @Test
    @DisplayName("Un PROVEEDOR no puede usar favoritos → 403")
    void proveedorRecibe403() throws Exception {
        marcar(tokenProveedor, taladroId).andExpect(status().isForbidden());
        mvc.perform(conToken(get("/api/favoritos/mios"), tokenProveedor)).andExpect(status().isForbidden());
        mvc.perform(conToken(delete("/api/favoritos/{id}", taladroId), tokenProveedor))
                .andExpect(status().isForbidden());
    }
}
