package com.rentaherramientas.domain.rental.exception;

import com.rentaherramientas.domain.rental.model.valueobject.EstadoReserva;
import com.rentaherramientas.domain.shared.exception.DomainException;

/**
 * Regla: solo una reserva PENDIENTE se puede reprogramar; una vez aceptada, el
 * proveedor ya se comprometió con esas fechas.
 */
public class ReservaNoReprogramableException extends DomainException {

    /** Crea la excepción indicando el estado en que está la reserva. */
    public ReservaNoReprogramableException(EstadoReserva estadoActual) {
        super("Solo se pueden reprogramar reservas PENDIENTE; la reserva está " + estadoActual);
    }
}
