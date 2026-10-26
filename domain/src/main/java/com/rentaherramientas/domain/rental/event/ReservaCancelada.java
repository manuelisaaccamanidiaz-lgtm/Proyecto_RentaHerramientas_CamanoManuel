package com.rentaherramientas.domain.rental.event;

import java.time.LocalDateTime;

import com.rentaherramientas.domain.shared.event.DomainEvent;

/**
 * Se emite cuando el cliente cancela su reserva (p. ej. para avisar al proveedor
 * de que esas fechas quedaron libres).
 */
public record ReservaCancelada(Long reservaId,
                               Long herramientaId,
                               Long clienteId,
                               LocalDateTime ocurridoEn) implements DomainEvent {
}
