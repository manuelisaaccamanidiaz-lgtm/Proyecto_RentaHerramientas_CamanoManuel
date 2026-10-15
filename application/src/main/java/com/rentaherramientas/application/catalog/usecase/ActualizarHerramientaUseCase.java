package com.rentaherramientas.application.catalog.usecase;

import com.rentaherramientas.application.catalog.command.ActualizarHerramientaCommand;
import com.rentaherramientas.application.catalog.dto.HerramientaResponse;

/**
 * Cambia datos, tarifa y categorías de una herramienta (su proveedor o un ADMIN).
 */
public interface ActualizarHerramientaUseCase {

    HerramientaResponse actualizar(ActualizarHerramientaCommand command);
}
