package com.rentaherramientas.infrastructure.rental.adapters.in.rest;

import com.rentaherramientas.application.rental.command.CrearReservaCommand;
import com.rentaherramientas.application.shared.command.Solicitante;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Cuerpo de POST /api/reservas (fechas ISO yyyy-MM-dd, ambas incluidas). No
 * lleva clienteId: el cliente sale del JWT. Las reglas de fechas las aplica el caso de uso.
 */
public record CrearReservaRequest(
        @Schema(description = "Id de la herramienta a reservar", example = "7")
        @NotNull Long herramientaId,
        @Schema(description = "Primer día de alquiler (incluido), desde hoy en adelante", example = "2026-10-24")
        @NotNull LocalDate fechaInicio,
        @Schema(description = "Último día de alquiler (incluido); máximo 30 días en total", example = "2026-10-27")
        @NotNull LocalDate fechaFin) {

    /** Convierte el cuerpo HTTP en el command de aplicación, con el cliente tomado del JWT. */
    public CrearReservaCommand toCommand(Solicitante solicitante) {
        return new CrearReservaCommand(solicitante, herramientaId, fechaInicio, fechaFin);
    }
}
