package com.rentaherramientas.application.catalog.exception;

import com.rentaherramientas.application.shared.exception.ConflictoException;

/**
 * Se lanza al marcar como favorita una herramienta dada de baja (INACTIVA): existe,
 * pero ya no está publicada en el catálogo, así que el conflicto es con su estado (409).
 */
public class HerramientaNoFavoritableException extends ConflictoException {

    /** Crea la excepción indicando la herramienta inactiva. */
    public HerramientaNoFavoritableException(Long herramientaId) {
        super("La herramienta " + herramientaId + " está dada de baja y no se puede marcar como favorita");
    }
}
