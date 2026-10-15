package com.rentaherramientas.application.billing.exception;

import com.rentaherramientas.application.shared.exception.RecursoNoEncontradoException;

/**
 * Se lanza cuando se consulta o resuelve un pago que no existe.
 */
public class PagoNoEncontradoException extends RecursoNoEncontradoException {

    public PagoNoEncontradoException(Long id) {
        super("No existe el pago " + id);
    }
}
