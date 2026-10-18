package com.rentaherramientas.application.catalog.usecase;

import com.rentaherramientas.application.catalog.command.GuardarCategoriaCommand;
import com.rentaherramientas.application.catalog.dto.CategoriaResponse;

/**
 * Cambia nombre y descripción de una categoría (solo ADMIN).
 */
public interface ActualizarCategoriaUseCase {

    /** Actualiza la categoría indicada y devuelve el resultado. */
    CategoriaResponse actualizar(Long id, GuardarCategoriaCommand command);
}
