package com.rentaherramientas.domain.billing.exception;

import com.rentaherramientas.domain.shared.exception.DomainException;

/**
 * Reglas de la factura: al menos una línea y todas en la misma moneda, para que
 * el total calculado tenga sentido.
 */
public class FacturaInvalidaException extends DomainException {

    public FacturaInvalidaException(String mensaje) {
        super(mensaje);
    }
}
