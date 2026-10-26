package com.rentaherramientas.application.rental.command;

import com.rentaherramientas.application.shared.command.Solicitante;

import java.time.LocalDate;

/**
 * Intención de mover una reserva PENDIENTE a otras fechas (ambas incluidas). Solo
 * puede hacerlo el cliente que la creó, que es el solicitante autenticado.
 */
public record ReprogramarReservaCommand(Solicitante solicitante, Long reservaId, LocalDate fechaInicio,
                                        LocalDate fechaFin) {
}
