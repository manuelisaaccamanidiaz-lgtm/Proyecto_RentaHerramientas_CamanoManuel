package com.rentaherramientas.infrastructure.billing.adapters.in.rest;

import com.rentaherramientas.application.billing.command.PagarReservaCommand;
import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.domain.billing.model.valueobject.MetodoPago;
import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo de POST /api/pagos. A propósito NO tiene monto ni datos de tarjeta: el
 * monto lo calcula el servidor y el pago es simulado. Si el cliente envía un
 * campo "monto", Jackson lo ignora (no existe en este record).
 */
public record PagarReservaRequest(@NotNull Long reservaId, @NotNull MetodoPago metodo) {

    public PagarReservaCommand toCommand(Solicitante solicitante) {
        return new PagarReservaCommand(solicitante, reservaId, metodo);
    }
}
