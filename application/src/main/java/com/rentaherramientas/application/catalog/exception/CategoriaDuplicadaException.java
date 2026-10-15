package com.rentaherramientas.application.catalog.exception;

import com.rentaherramientas.application.shared.exception.ConflictoException;

/**
 * Se lanza al crear o renombrar una categoría con un nombre que ya usa otra.
 */
public class CategoriaDuplicadaException extends ConflictoException {

    public CategoriaDuplicadaException(String nombre) {
        super("Ya existe una categoría llamada " + nombre);
    }
}
