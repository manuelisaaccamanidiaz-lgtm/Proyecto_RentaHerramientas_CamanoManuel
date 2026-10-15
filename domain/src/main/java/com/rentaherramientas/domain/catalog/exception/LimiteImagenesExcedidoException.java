package com.rentaherramientas.domain.catalog.exception;

import com.rentaherramientas.domain.shared.exception.DomainException;

/**
 * Regla: una herramienta tiene como máximo un número fijo de imágenes en su galería.
 */
public class LimiteImagenesExcedidoException extends DomainException {

    public LimiteImagenesExcedidoException(int maximo) {
        super("Una herramienta admite como máximo " + maximo + " imágenes");
    }
}
