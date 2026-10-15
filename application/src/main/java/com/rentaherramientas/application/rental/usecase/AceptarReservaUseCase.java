package com.rentaherramientas.application.rental.usecase;

import com.rentaherramientas.application.rental.dto.ReservaResponse;
import com.rentaherramientas.application.shared.command.Solicitante;

/**
 * El proveedor de la herramienta (o un ADMIN) acepta una reserva PENDIENTE.
 */
public interface AceptarReservaUseCase {

    ReservaResponse aceptar(Long reservaId, Solicitante solicitante);
}
