package com.rentaherramientas.application.catalog.usecase;

import com.rentaherramientas.application.catalog.command.RegistrarHerramientaCommand;
import com.rentaherramientas.application.catalog.dto.HerramientaResponse;

/**
 * Alta de una herramienta por su proveedor.
 */
public interface RegistrarHerramientaUseCase {

    HerramientaResponse registrar(RegistrarHerramientaCommand command);
}
