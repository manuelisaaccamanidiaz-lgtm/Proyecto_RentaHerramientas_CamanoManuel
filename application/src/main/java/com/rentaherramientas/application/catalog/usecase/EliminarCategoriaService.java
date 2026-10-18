package com.rentaherramientas.application.catalog.usecase;

import com.rentaherramientas.application.catalog.exception.CategoriaNoEncontradaException;
import com.rentaherramientas.domain.catalog.port.repository.CategoriaRepository;

/**
 * Elimina una categoría. Si alguna herramienta la usa, la FK
 * fk_herramienta_categorias_categoria impide el borrado y el adaptador REST
 * responde 409: así la regla no depende de una comprobación previa con carreras.
 */
public class EliminarCategoriaService implements EliminarCategoriaUseCase {

    private final CategoriaRepository categoriaRepository;

    /** Recibe sus dependencias por constructor; el bean lo crea CatalogBeansConfig. */
    public EliminarCategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    /** Elimina la categoría; lanza 404 si no existe. */
    @Override
    public void eliminar(Long id) {
        if (categoriaRepository.buscarPorId(id).isEmpty()) {
            throw new CategoriaNoEncontradaException(id);
        }
        categoriaRepository.eliminar(id);
    }
}
