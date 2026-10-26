package com.rentaherramientas.application.rental.exception;

import com.rentaherramientas.application.shared.exception.RecursoNoEncontradoException;

/**
 * Se lanza cuando se consulta o modifica una reserva que no existe.
 */
public class ReservaNoEncontradaException extends RecursoNoEncontradoException {

    /** Crea la excepción indicando el id que no existe. */
    public ReservaNoEncontradaException(Long id) {
        super("No existe la reserva " + id);
    }
}
