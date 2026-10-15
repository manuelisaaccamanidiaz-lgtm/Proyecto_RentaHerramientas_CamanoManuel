package com.rentaherramientas.application.rental.usecase;

import com.rentaherramientas.application.rental.command.CrearReservaCommand;
import com.rentaherramientas.application.rental.dto.ReservaResponse;

/**
 * Un CLIENTE solicita una reserva; queda PENDIENTE hasta que el proveedor responda.
 */
public interface CrearReservaUseCase {

    ReservaResponse crear(CrearReservaCommand command);
}
