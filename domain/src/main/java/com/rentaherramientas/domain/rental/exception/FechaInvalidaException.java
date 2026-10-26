package com.rentaherramientas.domain.rental.exception;

import com.rentaherramientas.domain.shared.exception.DomainException;

/**
 * Regla: la devolución no puede registrarse antes de la entrega
 * (ck_reservas_devolucion_tras_entrega).
 */
public class FechaInvalidaException extends DomainException {

    /** Crea la excepción con el motivo concreto por el que la fecha no es válida. */
    public FechaInvalidaException(String mensaje) {
        super(mensaje);
    }
}
