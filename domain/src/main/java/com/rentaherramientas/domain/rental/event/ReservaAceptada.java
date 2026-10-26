package com.rentaherramientas.domain.rental.event;

import java.time.LocalDateTime;

import com.rentaherramientas.domain.shared.event.DomainEvent;

/**
 * Se emite cuando el proveedor acepta una solicitud (p. ej. para avisar al
 * cliente de que ya puede pagarla).
 */
public record ReservaAceptada(Long reservaId,
                              Long herramientaId,
                              Long clienteId,
                              LocalDateTime ocurridoEn) implements DomainEvent {
}
