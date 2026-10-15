package com.rentaherramientas.infrastructure.rental.adapters.in.rest;

import com.rentaherramientas.application.rental.command.ReportarDanioCommand;
import com.rentaherramientas.application.shared.command.Solicitante;
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
        @NotBlank @Size(max = 2000) String descripcion,
        @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal costoEstimado) {

    public ReportarDanioCommand toCommand(Long reservaId, Solicitante solicitante) {
        return new ReportarDanioCommand(reservaId, solicitante, descripcion, costoEstimado);
    }
}
