package com.rentaherramientas.infrastructure.rental.adapters.in.rest;

import com.rentaherramientas.application.rental.command.ReprogramarReservaCommand;
import com.rentaherramientas.application.shared.command.Solicitante;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Cuerpo de PATCH /api/reservas/{id}/reprogramar (fechas ISO yyyy-MM-dd, ambas
 * incluidas). Las reglas de fechas las aplica el caso de uso, igual que al crear.
 */
public record ReprogramarReservaRequest(
        @Schema(description = "Nuevo primer día de alquiler (incluido), desde hoy en adelante", example = "2026-11-03")
        @NotNull LocalDate fechaInicio,
        @Schema(description = "Nuevo último día de alquiler (incluido); máximo 30 días en total", example = "2026-11-05")
        @NotNull LocalDate fechaFin) {

    /** Convierte el cuerpo HTTP en el command de aplicación, con el cliente tomado del JWT. */
    public ReprogramarReservaCommand toCommand(Long reservaId, Solicitante solicitante) {
        return new ReprogramarReservaCommand(solicitante, reservaId, fechaInicio, fechaFin);
    }
}
