package com.rentaherramientas.application.catalog.usecase;

import com.rentaherramientas.application.catalog.dto.CategoriaResponse;

import java.util.List;

/**
 * Consultas públicas de categorías.
 */
public interface ConsultarCategoriaUseCase {

    /** Devuelve la categoría con ese id. */
    CategoriaResponse buscarPorId(Long id);

    /** Devuelve todas las categorías. */
    List<CategoriaResponse> listar();
}
