package com.rentaherramientas.domain.rental.event;

import java.time.LocalDateTime;

import com.rentaherramientas.domain.shared.event.DomainEvent;

/**
 * Se emite cuando el cliente devuelve la herramienta; billing puede escucharlo
 * para cerrar cobros y catalog para revisar el estado de la unidad.
 */
public record ReservaDevuelta(Long reservaId,
                              Long herramientaId,
                              LocalDateTime ocurridoEn) implements DomainEvent {
}
