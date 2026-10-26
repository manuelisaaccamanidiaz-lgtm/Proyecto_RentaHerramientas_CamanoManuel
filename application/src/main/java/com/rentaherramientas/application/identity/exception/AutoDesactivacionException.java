package com.rentaherramientas.application.identity.exception;

import com.rentaherramientas.application.shared.exception.ConflictoException;

/**
 * Se lanza cuando un ADMIN intenta desactivarse a sí mismo: podría dejar el
 * sistema sin ningún administrador activo.
 */
public class AutoDesactivacionException extends ConflictoException {

    /** Crea la excepción con un mensaje fijo. */
    public AutoDesactivacionException() {
        super("Un administrador no puede desactivarse a sí mismo");
    }
}
