package com.rentaherramientas.domain.rental.event;

import java.time.LocalDateTime;

import com.rentaherramientas.domain.shared.event.DomainEvent;

/**
 * Se emite cuando el proveedor rechaza una solicitud (p. ej. para avisar al cliente).
 */
public record ReservaRechazada(Long reservaId,
                               Long herramientaId,
                               Long clienteId,
                               LocalDateTime ocurridoEn) implements DomainEvent {
}
