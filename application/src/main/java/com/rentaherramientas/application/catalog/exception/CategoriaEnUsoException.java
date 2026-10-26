package com.rentaherramientas.application.catalog.exception;

import com.rentaherramientas.application.shared.exception.ConflictoException;

/**
 * Se lanza al intentar eliminar una categoría que todavía usa alguna herramienta.
 */
public class CategoriaEnUsoException extends ConflictoException {

    /** Crea la excepción indicando la categoría en uso. */
    public CategoriaEnUsoException(String nombre) {
        super("No se puede eliminar la categoría " + nombre
                + " porque tiene herramientas asociadas; quítala de esas herramientas primero");
    }
}
