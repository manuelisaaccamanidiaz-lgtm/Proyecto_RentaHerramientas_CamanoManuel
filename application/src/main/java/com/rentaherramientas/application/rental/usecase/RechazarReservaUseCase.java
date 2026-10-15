package com.rentaherramientas.application.rental.usecase;

import com.rentaherramientas.application.rental.dto.ReservaResponse;
import com.rentaherramientas.application.shared.command.Solicitante;

/**
 * El proveedor de la herramienta (o un ADMIN) rechaza una reserva PENDIENTE.
 */
public interface RechazarReservaUseCase {

    ReservaResponse rechazar(Long reservaId, Solicitante solicitante);
}
