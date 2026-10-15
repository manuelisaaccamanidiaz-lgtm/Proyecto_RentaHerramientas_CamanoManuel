package com.rentaherramientas.domain.rental.exception;

import com.rentaherramientas.domain.rental.model.valueobject.EstadoReserva;
import com.rentaherramientas.domain.shared.exception.DomainException;

/**
 * Regla: los daños solo se reportan sobre una reserva DEVUELTA, que es cuando
 * se inspecciona la herramienta.
 */
public class DanioNoPermitidoException extends DomainException {

    public DanioNoPermitidoException(EstadoReserva estadoActual) {
        super("Solo se pueden reportar daños en reservas DEVUELTA; la reserva está " + estadoActual);
    }
}
