package com.rentaherramientas.infrastructure.billing.adapters.in.rest;

import com.rentaherramientas.application.billing.command.PagarReservaCommand;
import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.domain.billing.model.valueobject.MetodoPago;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo de POST /api/pagos. A propósito NO tiene monto ni datos de tarjeta: el
 * monto lo calcula el servidor y el pago es simulado. Si el cliente envía un
 * campo "monto", Jackson lo ignora (no existe en este record).
 */
public record PagarReservaRequest(
        @Schema(description = "Id de la reserva ACEPTADA que se va a pagar", example = "15")
        @NotNull Long reservaId,
        @Schema(description = "Método de pago simulado: TARJETA y PSE se aprueban al instante, EFECTIVO queda PENDIENTE",
                example = "PSE")
        @NotNull MetodoPago metodo) {

    /** Convierte el cuerpo HTTP en el command de aplicación, con el cliente tomado del JWT. */
    public PagarReservaCommand toCommand(Solicitante solicitante) {
        return new PagarReservaCommand(solicitante, reservaId, metodo);
    }
}
