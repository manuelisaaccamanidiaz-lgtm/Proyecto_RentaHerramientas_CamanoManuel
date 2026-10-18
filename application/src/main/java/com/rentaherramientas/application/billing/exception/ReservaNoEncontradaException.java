package com.rentaherramientas.application.billing.exception;

import com.rentaherramientas.application.shared.exception.RecursoNoEncontradoException;

/**
 * Se lanza al pagar (o consultar el pago de) una reserva que no existe. Es propia
 * de billing para no depender de las excepciones de rental.
 */
public class ReservaNoEncontradaException extends RecursoNoEncontradoException {

    /** Crea la excepción indicando el id que no existe. */
    public ReservaNoEncontradaException(Long id) {
        super("No existe la reserva " + id);
    }
}
