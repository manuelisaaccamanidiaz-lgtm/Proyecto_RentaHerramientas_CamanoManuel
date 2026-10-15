package com.rentaherramientas.domain.catalog.exception;

import com.rentaherramientas.domain.catalog.model.valueobject.EstadoHerramienta;
import com.rentaherramientas.domain.shared.exception.DomainException;

/**
 * Regla: el estado de una herramienta solo cambia siguiendo las transiciones
 * permitidas por {@link EstadoHerramienta#puedeCambiarA}.
 */
public class TransicionEstadoInvalidaException extends DomainException {

    public TransicionEstadoInvalidaException(EstadoHerramienta origen, EstadoHerramienta destino) {
        super("La herramienta no puede pasar de " + origen + " a " + destino);
    }
}
