package com.rentaherramientas.application.rental.port;

import com.rentaherramientas.domain.shared.model.valueobject.Dinero;

/**
 * Vista de una herramienta desde rental: quién es su proveedor, cuánto cuesta
 * por día y si está operativa (DISPONIBLE) para recibir reservas nuevas.
 */
public record HerramientaReservable(Long id, Long proveedorId, Dinero tarifaDiaria, boolean disponible) {
}
