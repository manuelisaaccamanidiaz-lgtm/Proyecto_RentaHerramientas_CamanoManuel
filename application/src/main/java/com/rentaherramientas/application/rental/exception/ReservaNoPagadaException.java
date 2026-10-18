package com.rentaherramientas.application.rental.exception;

import com.rentaherramientas.application.shared.exception.ConflictoException;

/**
 * Se lanza al registrar la entrega de una reserva que aún no tiene un pago
 * APROBADO: la herramienta no sale sin estar pagada.
 */
public class ReservaNoPagadaException extends ConflictoException {

    /** Crea la excepción para una reserva que se intenta entregar sin pago aprobado. */
    public ReservaNoPagadaException(Long reservaId) {
        super("La reserva " + reservaId + " no tiene un pago aprobado; no se puede entregar la herramienta");
    }
}
