package com.rentaherramientas.application.rental.command;

import com.rentaherramientas.application.shared.command.Solicitante;

import java.time.LocalDate;

/**
 * Intención de reservar una herramienta entre dos fechas (ambas incluidas). El
 * cliente es siempre el solicitante autenticado.
 */
public record CrearReservaCommand(Solicitante solicitante, Long herramientaId, LocalDate fechaInicio,
                                  LocalDate fechaFin) {
}
