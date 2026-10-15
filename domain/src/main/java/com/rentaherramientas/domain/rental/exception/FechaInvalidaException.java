package com.rentaherramientas.domain.rental.exception;

import com.rentaherramientas.domain.shared.exception.DomainException;

/**
 * Regla: la devolución no puede registrarse antes de la entrega
 * (ck_reservas_devolucion_tras_entrega).
 */
public class FechaInvalidaException extends DomainException {

    public FechaInvalidaException(String mensaje) {
        super(mensaje);
    }
}
