package com.rentaherramientas.application.catalog.dto;

import com.rentaherramientas.domain.catalog.model.aggregate.Categoria;

/**
 * Vista pública de una categoría.
 */
public record CategoriaResponse(Long id, String nombre, String descripcion) {

    public static CategoriaResponse desde(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNombre(), categoria.getDescripcion());
    }
}
