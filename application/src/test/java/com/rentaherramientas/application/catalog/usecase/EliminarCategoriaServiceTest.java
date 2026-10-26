package com.rentaherramientas.application.catalog.usecase;

import com.rentaherramientas.application.catalog.exception.CategoriaEnUsoException;
import com.rentaherramientas.application.catalog.exception.CategoriaNoEncontradaException;
import com.rentaherramientas.domain.catalog.model.aggregate.Categoria;
import com.rentaherramientas.domain.catalog.port.repository.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del caso de uso con un repositorio de categorías en memoria (sin
 * Mockito): el conjunto "enUso" hace las veces de la tabla herramienta_categorias.
 */
class EliminarCategoriaServiceTest {

    private static final Long EN_USO_ID = 1L;
    private static final Long LIBRE_ID = 2L;

    private CategoriaRepositoryEnMemoria categorias;
    private EliminarCategoriaService service;

    @BeforeEach
    void preparar() {
        categorias = new CategoriaRepositoryEnMemoria();
        categorias.guardar(Categoria.reconstruir(EN_USO_ID, "Corte", null));
        categorias.guardar(Categoria.reconstruir(LIBRE_ID, "Jardinería", null));
        categorias.enUso.add(EN_USO_ID);
        service = new EliminarCategoriaService(categorias);
    }

    @Test
    @DisplayName("Una categoría usada por alguna herramienta → CategoriaEnUsoException (409) y no se borra")
    void enUsoNoSeElimina() {
        CategoriaEnUsoException ex = assertThrows(CategoriaEnUsoException.class, () -> service.eliminar(EN_USO_ID));

        assertTrue(ex.getMessage().contains("Corte"));
        assertTrue(categorias.buscarPorId(EN_USO_ID).isPresent());
    }

    @Test
    @DisplayName("Una categoría libre se elimina")
    void libreSeElimina() {
        service.eliminar(LIBRE_ID);

        assertFalse(categorias.buscarPorId(LIBRE_ID).isPresent());
    }

    @Test
    @DisplayName("Una categoría que no existe → CategoriaNoEncontradaException (404)")
    void inexistente() {
        assertThrows(CategoriaNoEncontradaException.class, () -> service.eliminar(999L));
    }

    // ------------------------------------------------------- dobles de prueba

    /** Categorías en memoria por id; "enUso" simula las filas de herramienta_categorias. */
    private static final class CategoriaRepositoryEnMemoria implements CategoriaRepository {

        private final Map<Long, Categoria> porId = new LinkedHashMap<>();
        private final Set<Long> enUso = new HashSet<>();

        @Override
        public Categoria guardar(Categoria categoria) {
            porId.put(categoria.getId(), categoria);
            return categoria;
        }

        @Override
        public Optional<Categoria> buscarPorId(Long id) {
            return Optional.ofNullable(porId.get(id));
        }

        @Override
        public List<Categoria> buscarTodas() {
            return porId.values().stream().sorted(Comparator.comparing(Categoria::getNombre)).toList();
        }

        @Override
        public boolean existePorNombre(String nombre) {
            return buscarPorNombre(nombre).isPresent();
        }

        @Override
        public Optional<Categoria> buscarPorNombre(String nombre) {
            return porId.values().stream().filter(c -> c.getNombre().equals(nombre)).findFirst();
        }

        @Override
        public boolean existenTodas(Collection<Long> ids) {
            return porId.keySet().containsAll(ids);
        }

        @Override
        public boolean estaEnUso(Long categoriaId) {
            return enUso.contains(categoriaId);
        }

        @Override
        public void eliminar(Long id) {
            porId.remove(id);
        }
    }
}
