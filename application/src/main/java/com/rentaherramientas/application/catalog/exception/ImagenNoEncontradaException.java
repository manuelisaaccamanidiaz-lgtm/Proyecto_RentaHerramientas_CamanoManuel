package com.rentaherramientas.application.catalog.exception;

import com.rentaherramientas.application.shared.exception.RecursoNoEncontradoException;

/**
 * Se lanza al quitar de una herramienta una imagen cuya posición no existe.
 */
public class ImagenNoEncontradaException extends RecursoNoEncontradoException {

    /** Crea la excepción indicando la herramienta y la posición de imagen que no existe. */
    public ImagenNoEncontradaException(Long herramientaId, int orden) {
        super("La herramienta " + herramientaId + " no tiene imagen en la posición " + orden);
    }
}
