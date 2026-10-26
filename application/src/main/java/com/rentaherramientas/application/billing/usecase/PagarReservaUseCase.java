package com.rentaherramientas.application.billing.usecase;

import com.rentaherramientas.application.billing.command.PagarReservaCommand;
import com.rentaherramientas.application.billing.dto.PagoResponse;

/**
 * Un CLIENTE paga su reserva ACEPTADA (pago simulado).
 */
public interface PagarReservaUseCase {

    /** Registra el pago de una reserva y devuelve el pago (con factura si quedó aprobado). */
    PagoResponse pagar(PagarReservaCommand command);
}
