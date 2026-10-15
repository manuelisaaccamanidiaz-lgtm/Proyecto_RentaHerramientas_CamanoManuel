package com.rentaherramientas.application.catalog.usecase;

import com.rentaherramientas.application.catalog.dto.CategoriaResponse;

import java.util.List;

/**
 * Consultas públicas de categorías.
 */
public interface ConsultarCategoriaUseCase {

    CategoriaResponse buscarPorId(Long id);

    List<CategoriaResponse> listar();
}
