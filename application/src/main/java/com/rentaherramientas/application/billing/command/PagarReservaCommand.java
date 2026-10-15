package com.rentaherramientas.application.billing.command;

import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.domain.billing.model.valueobject.MetodoPago;

/**
 * Intención de pagar una reserva. No lleva monto: lo calcula el servidor a partir
 * de la reserva, para que el cliente no pueda pagar menos de lo pactado. Tampoco
 * lleva datos de tarjeta: el pago es simulado y la plataforma nunca los guarda.
 */
public record PagarReservaCommand(Solicitante solicitante, Long reservaId, MetodoPago metodo) {
}
