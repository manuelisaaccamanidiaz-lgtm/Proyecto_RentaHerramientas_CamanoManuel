package com.rentaherramientas.infrastructure.rental.adapters.in.rest;

import com.rentaherramientas.application.rental.command.CrearReservaCommand;
import com.rentaherramientas.application.shared.command.Solicitante;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Cuerpo de POST /api/reservas (fechas ISO yyyy-MM-dd, ambas incluidas). No
 * lleva clienteId: el cliente sale del JWT. Las reglas de fechas las aplica el caso de uso.
 */
public record CrearReservaRequest(
        @NotNull Long herramientaId,
        @NotNull LocalDate fechaInicio,
        @NotNull LocalDate fechaFin) {

    /** Convierte el cuerpo HTTP en el command de aplicación, con el cliente tomado del JWT. */
    public CrearReservaCommand toCommand(Solicitante solicitante) {
        return new CrearReservaCommand(solicitante, herramientaId, fechaInicio, fechaFin);
    }
}
