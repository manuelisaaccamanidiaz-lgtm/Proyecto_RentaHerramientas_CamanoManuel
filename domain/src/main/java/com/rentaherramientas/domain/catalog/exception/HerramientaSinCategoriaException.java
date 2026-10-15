package com.rentaherramientas.domain.catalog.exception;

import com.rentaherramientas.domain.shared.exception.DomainException;

/**
 * Regla: toda herramienta pertenece al menos a una categoría, para que siempre
 * sea localizable en el catálogo.
 */
public class HerramientaSinCategoriaException extends DomainException {

    public HerramientaSinCategoriaException() {
        super("La herramienta debe pertenecer al menos a una categoría");
    }
}
