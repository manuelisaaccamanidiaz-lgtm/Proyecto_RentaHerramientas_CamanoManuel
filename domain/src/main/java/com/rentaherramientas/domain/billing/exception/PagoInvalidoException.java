package com.rentaherramientas.domain.billing.exception;

import com.rentaherramientas.domain.shared.exception.DomainException;

/**
 * Reglas del pago: monto mayor que cero y solo se resuelve (aprobar/rechazar)
 * mientras está PENDIENTE; un pago resuelto ya no cambia.
 */
public class PagoInvalidoException extends DomainException {

    public PagoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
