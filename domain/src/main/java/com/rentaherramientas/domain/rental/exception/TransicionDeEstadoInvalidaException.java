package com.rentaherramientas.domain.rental.exception;

import com.rentaherramientas.domain.rental.model.valueobject.EstadoReserva;
import com.rentaherramientas.domain.shared.exception.DomainException;

/**
 * Regla: el estado de una reserva solo cambia siguiendo las transiciones
 * permitidas por {@link EstadoReserva#puedeCambiarA}.
 */
public class TransicionDeEstadoInvalidaException extends DomainException {

    /** Crea la excepción indicando el estado actual y el estado al que se intentó pasar. */
    public TransicionDeEstadoInvalidaException(EstadoReserva origen, EstadoReserva destino) {
        super("La reserva no puede pasar de " + origen + " a " + destino);
    }
}
