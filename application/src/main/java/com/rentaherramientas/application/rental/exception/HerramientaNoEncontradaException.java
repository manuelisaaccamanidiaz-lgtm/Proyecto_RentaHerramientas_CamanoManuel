package com.rentaherramientas.application.rental.exception;

import com.rentaherramientas.application.shared.exception.RecursoNoEncontradoException;

/**
 * Se lanza cuando se intenta reservar (o consultar la disponibilidad de) una
 * herramienta que no existe. Es propia de rental para no depender de catalog.
 */
public class HerramientaNoEncontradaException extends RecursoNoEncontradoException {

    /** Crea la excepción indicando el id que no existe. */
    public HerramientaNoEncontradaException(Long id) {
        super("No existe la herramienta " + id);
    }
}
