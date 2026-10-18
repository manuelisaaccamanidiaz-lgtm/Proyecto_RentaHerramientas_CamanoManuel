package com.rentaherramientas.application.catalog.exception;

import com.rentaherramientas.application.shared.exception.RecursoNoEncontradoException;

/**
 * Se lanza cuando se consulta o modifica una herramienta que no existe.
 */
public class HerramientaNoEncontradaException extends RecursoNoEncontradoException {

    /** Crea la excepción indicando el id que no existe. */
    public HerramientaNoEncontradaException(Long id) {
        super("No existe la herramienta " + id);
    }
}
