package com.rentaherramientas.application.catalog.usecase;

/**
 * Borrado físico de una categoría que ninguna herramienta usa (solo ADMIN).
 */
public interface EliminarCategoriaUseCase {

    /** Elimina la categoría indicada. */
    void eliminar(Long id);
}
