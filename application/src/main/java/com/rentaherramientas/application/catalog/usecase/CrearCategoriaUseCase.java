package com.rentaherramientas.application.catalog.usecase;

import com.rentaherramientas.application.catalog.command.GuardarCategoriaCommand;
import com.rentaherramientas.application.catalog.dto.CategoriaResponse;

/**
 * Alta de categorías (solo ADMIN).
 */
public interface CrearCategoriaUseCase {

    /** Crea una categoría y devuelve sus datos con el id asignado. */
    CategoriaResponse crear(GuardarCategoriaCommand command);
}
