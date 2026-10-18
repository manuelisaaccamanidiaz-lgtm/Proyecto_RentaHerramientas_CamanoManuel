package com.rentaherramientas.application.billing.exception;

import com.rentaherramientas.application.shared.exception.ConflictoException;

/**
 * Se lanza al pagar (o confirmar el pago de) una reserva que no está ACEPTADA:
 * solo se cobra lo que el proveedor ya aceptó y aún no se ha entregado.
 */
public class ReservaNoPagableException extends ConflictoException {

    /** Crea la excepción indicando la reserva y el estado que impide pagarla. */
    public ReservaNoPagableException(Long reservaId, String estado) {
        super("Solo se pueden pagar reservas ACEPTADAS; la reserva " + reservaId + " está " + estado);
    }
}
