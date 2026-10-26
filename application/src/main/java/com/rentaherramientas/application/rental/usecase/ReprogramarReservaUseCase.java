package com.rentaherramientas.application.rental.usecase;

import com.rentaherramientas.application.rental.command.ReprogramarReservaCommand;
import com.rentaherramientas.application.rental.dto.ReservaResponse;

/**
 * Puerto de entrada: el cliente cambia las fechas de su reserva PENDIENTE.
 */
public interface ReprogramarReservaUseCase {

    /** Reprograma la reserva y la devuelve actualizada, con el nuevo precio total. */
    ReservaResponse reprogramar(ReprogramarReservaCommand command);
}
