package com.rentaherramientas.domain.rental.event;

import java.time.LocalDateTime;

import com.rentaherramientas.domain.shared.event.DomainEvent;

/**
 * Se emite al entregar la herramienta al cliente: desde ese momento la unidad
 * está fuera del almacén del proveedor.
 */
public record ReservaEntregada(Long reservaId,
                               Long herramientaId,
                               Long clienteId,
                               LocalDateTime ocurridoEn) implements DomainEvent {
}
