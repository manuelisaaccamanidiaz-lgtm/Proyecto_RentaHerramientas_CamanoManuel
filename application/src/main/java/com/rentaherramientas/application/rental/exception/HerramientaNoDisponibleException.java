package com.rentaherramientas.application.rental.exception;

import com.rentaherramientas.application.shared.exception.ConflictoException;

/**
 * Se lanza al reservar una herramienta que no está operativa (en mantenimiento,
 * dañada o dada de baja).
 */
public class HerramientaNoDisponibleException extends ConflictoException {

    public HerramientaNoDisponibleException(Long id) {
        super("La herramienta " + id + " no está disponible para reservar");
    }
}
