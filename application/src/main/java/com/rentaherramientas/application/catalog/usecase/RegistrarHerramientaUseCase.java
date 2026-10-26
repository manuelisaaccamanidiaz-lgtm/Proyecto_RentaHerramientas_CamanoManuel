package com.rentaherramientas.application.catalog.usecase;

import com.rentaherramientas.application.catalog.command.RegistrarHerramientaCommand;
import com.rentaherramientas.application.catalog.dto.HerramientaResponse;

/**
 * Alta de una herramienta por su proveedor.
 */
public interface RegistrarHerramientaUseCase {

    /** Registra una herramienta nueva del proveedor solicitante. */
    HerramientaResponse registrar(RegistrarHerramientaCommand command);
}
