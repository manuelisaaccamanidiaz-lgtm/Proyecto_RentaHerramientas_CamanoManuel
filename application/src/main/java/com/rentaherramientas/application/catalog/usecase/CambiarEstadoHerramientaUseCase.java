package com.rentaherramientas.application.catalog.usecase;

import com.rentaherramientas.application.catalog.command.CambiarEstadoHerramientaCommand;
import com.rentaherramientas.application.catalog.dto.HerramientaResponse;
import com.rentaherramientas.application.shared.command.Solicitante;

/**
 * Cambios de estado operativo de una herramienta y su baja lógica.
 */
public interface CambiarEstadoHerramientaUseCase {

    HerramientaResponse cambiarEstado(CambiarEstadoHerramientaCommand command);

    /** "Eliminar" = inactivar: reservas e historiales siguen referenciando la herramienta. */
    void eliminar(Long herramientaId, Solicitante solicitante);
}
