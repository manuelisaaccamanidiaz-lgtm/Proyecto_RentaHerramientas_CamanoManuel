package com.rentaherramientas.application.catalog.usecase;

/**
 * Borrado físico de una categoría que ninguna herramienta usa (solo ADMIN).
 */
public interface EliminarCategoriaUseCase {

    void eliminar(Long id);
}
