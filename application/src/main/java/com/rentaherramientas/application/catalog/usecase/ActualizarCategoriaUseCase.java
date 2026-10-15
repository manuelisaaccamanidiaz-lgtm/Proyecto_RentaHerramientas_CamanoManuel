package com.rentaherramientas.application.catalog.usecase;

import com.rentaherramientas.application.catalog.command.GuardarCategoriaCommand;
import com.rentaherramientas.application.catalog.dto.CategoriaResponse;

/**
 * Cambia nombre y descripción de una categoría (solo ADMIN).
 */
public interface ActualizarCategoriaUseCase {

    CategoriaResponse actualizar(Long id, GuardarCategoriaCommand command);
}
