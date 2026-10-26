package com.rentaherramientas.domain.rental.exception;

import com.rentaherramientas.domain.rental.model.valueobject.RangoFechas;
import com.rentaherramientas.domain.shared.exception.DomainException;

/**
 * Regla: una herramienta (una unidad física) no puede tener dos reservas activas
 * con fechas que se crucen. Involucra varias reservas, así que la comprueba el
 * caso de uso con {@code ReservaRepository#existeSolapamiento} antes de registrar.
 */
public class ReservaSolapadaException extends DomainException {

    /** Crea la excepción indicando la herramienta y el rango de fechas que choca con otra reserva. */
    public ReservaSolapadaException(Long herramientaId, RangoFechas rango) {
        super("La herramienta " + herramientaId + " ya está reservada entre "
                + rango.inicio() + " y " + rango.fin());
    }
}
