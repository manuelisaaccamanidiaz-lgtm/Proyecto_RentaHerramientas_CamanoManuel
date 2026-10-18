package com.rentaherramientas.application.rental.usecase;

import com.rentaherramientas.application.rental.dto.ReservaResponse;
import com.rentaherramientas.application.shared.command.Solicitante;

/**
 * El proveedor (o un ADMIN) registra que entregó la herramienta de una reserva ACEPTADA.
 */
public interface RegistrarEntregaUseCase {

    /** Registra la entrega de la reserva indicada. */
    ReservaResponse entregar(Long reservaId, Solicitante solicitante);
}
