package com.rentaherramientas;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Borrado de categorías contra PostgreSQL real. El perfil "test" no carga la
 * semilla V100, así que la prueba crea la categoría y una herramienta que la usa
 * (el mismo caso que una categoría de la semilla con herramientas).
 */
class CategoriaIntegrationTest extends IntegracionBase {

    private String tokenAdmin;

    @BeforeEach
    void prepararDatos() throws Exception {
        tokenAdmin = login("admin@pruebas.test", "Admin123*");
    }

    private long crearCategoria() throws Exception {
        return leerId(mvc.perform(conJson(post("/api/categorias"), tokenAdmin, """
                {"nombre": "Categoría %s", "descripcion": "Creada por la prueba"}
                """.formatted(System.nanoTime())))
                .andExpect(status().isCreated()));
    }

    @Test
    @DisplayName("una categoría con herramientas no se elimina: DELETE responde 409 y sigue existiendo")
    void categoriaEnUsoResponde409() throws Exception {
        long categoriaId = crearCategoria();
        String emailProveedor = emailUnico("proveedor");
        registrarProveedor(emailProveedor).andExpect(status().isCreated());
        String tokenProveedor = login(emailProveedor, CLAVE);
        mvc.perform(conJson(post("/api/herramientas"), tokenProveedor, """
                {"nombre": "Sierra de prueba", "tarifaDiaria": 20000, "categoriaIds": [%d]}
                """.formatted(categoriaId)))
                .andExpect(status().isCreated());

        mvc.perform(conToken(delete("/api/categorias/{id}", categoriaId), tokenAdmin))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(containsString("tiene herramientas asociadas")));

        mvc.perform(get("/api/categorias/{id}", categoriaId)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("una categoría sin herramientas se elimina: 204 y luego 404")
    void categoriaLibreSeElimina() throws Exception {
        long categoriaId = crearCategoria();

        mvc.perform(conToken(delete("/api/categorias/{id}", categoriaId), tokenAdmin))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/categorias/{id}", categoriaId)).andExpect(status().isNotFound());
    }
}
