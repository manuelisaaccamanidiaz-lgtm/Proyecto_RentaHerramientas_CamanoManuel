package com.rentaherramientas.application.catalog.command;

import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.domain.catalog.model.valueobject.EstadoHerramienta;

/**
 * Intención de llevar una herramienta a otro estado; si la transición está
 * permitida lo decide el agregado.
 */
public record CambiarEstadoHerramientaCommand(Long herramientaId, Solicitante solicitante,
                                              EstadoHerramienta estado) {
}
