package com.rentaherramientas.domain.catalog.exception;

import com.rentaherramientas.domain.shared.exception.DomainException;

/**
 * Regla: dentro de una herramienta cada imagen ocupa una posición (orden) única,
 * igual que la restricción uq_herramienta_imagenes_orden en la base de datos.
 */
public class ImagenDuplicadaException extends DomainException {

    /** Crea la excepción indicando la posición que ya está ocupada. */
    public ImagenDuplicadaException(int orden) {
        super("Ya existe una imagen en la posición " + orden);
    }
}
