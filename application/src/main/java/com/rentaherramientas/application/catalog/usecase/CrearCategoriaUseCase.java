package com.rentaherramientas.application.catalog.usecase;

import com.rentaherramientas.application.catalog.command.GuardarCategoriaCommand;
import com.rentaherramientas.application.catalog.dto.CategoriaResponse;

/**
 * Alta de categorías (solo ADMIN).
 */
public interface CrearCategoriaUseCase {

    CategoriaResponse crear(GuardarCategoriaCommand command);
}
