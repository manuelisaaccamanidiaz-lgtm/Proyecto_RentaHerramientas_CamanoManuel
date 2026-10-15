package com.rentaherramientas.domain.catalog.exception;

import com.rentaherramientas.domain.shared.exception.DomainException;

/**
 * Regla: la tarifa diaria debe ser mayor que cero. Dinero admite cero (sirve para
 * totales), pero alquilar gratis no es un caso de negocio válido en el catálogo.
 */
public class TarifaInvalidaException extends DomainException {

    public TarifaInvalidaException() {
        super("La tarifa diaria debe ser mayor que cero");
    }
}
