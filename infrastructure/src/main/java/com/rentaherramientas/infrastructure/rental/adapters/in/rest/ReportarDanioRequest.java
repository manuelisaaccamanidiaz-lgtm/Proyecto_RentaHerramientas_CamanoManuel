package com.rentaherramientas.infrastructure.rental.adapters.in.rest;

import com.rentaherramientas.application.rental.command.ReportarDanioCommand;
import com.rentaherramientas.application.shared.command.Solicitante;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Cuerpo de POST /api/reservas/{id}/danios. costoEstimado es opcional y va en la
 * moneda de la reserva.
 */
public record ReportarDanioRequest(
        @Schema(description = "Qué se dañó y cómo se encontró al recibir la herramienta",
                example = "Mandril del taladro con juego y cable de alimentación pelado cerca del enchufe")
        @NotBlank @Size(max = 2000) String descripcion,
        @Schema(description = "Costo estimado de la reparación en la moneda de la reserva (opcional)",
                example = "85000.00")
        @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal costoEstimado) {

    /** Convierte el cuerpo HTTP en el command de aplicación para la reserva indicada. */
    public ReportarDanioCommand toCommand(Long reservaId, Solicitante solicitante) {
        return new ReportarDanioCommand(reservaId, solicitante, descripcion, costoEstimado);
    }
}
