package com.rentaherramientas.application.shared.exception;

/**
 * Base de las excepciones de aplicación que chocan con el estado actual de los
 * datos (un valor único ya usado, una operación no admitida sobre el recurso).
 * El adaptador REST las traduce a 409.
 */
public abstract class ConflictoException extends RuntimeException {

    /** Constructor protegido: solo lo usan las subclases concretas (se traducen a HTTP 409). */
    protected ConflictoException(String mensaje) {
        super(mensaje);
    }
}
