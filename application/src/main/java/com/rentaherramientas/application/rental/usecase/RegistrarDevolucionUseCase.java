package com.rentaherramientas.application.rental.usecase;

import com.rentaherramientas.application.rental.dto.ReservaResponse;
import com.rentaherramientas.application.shared.command.Solicitante;

/**
 * El proveedor (o un ADMIN) registra que recibió de vuelta la herramienta de una reserva ENTREGADA.
 */
public interface RegistrarDevolucionUseCase {

    ReservaResponse devolver(Long reservaId, Solicitante solicitante);
}
