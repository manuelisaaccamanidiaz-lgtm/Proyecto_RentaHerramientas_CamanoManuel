package com.rentaherramientas.application.billing.exception;

import com.rentaherramientas.application.shared.exception.RecursoNoEncontradoException;

/**
 * Se lanza cuando se consulta o descarga una factura que no existe.
 */
public class FacturaNoEncontradaException extends RecursoNoEncontradoException {

    /** Crea la excepción indicando el id que no existe. */
    public FacturaNoEncontradaException(Long id) {
        super("No existe la factura " + id);
    }
}
