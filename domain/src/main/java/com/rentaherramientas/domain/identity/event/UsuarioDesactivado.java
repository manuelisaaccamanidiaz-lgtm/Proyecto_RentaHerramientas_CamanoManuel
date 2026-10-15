package com.rentaherramientas.domain.identity.event;

import com.rentaherramientas.domain.shared.event.DomainEvent;

import java.time.LocalDateTime;

/**
 * Se emite al desactivar un usuario; otros BC (catalog, rental) pueden reaccionar,
 * por ejemplo inactivando las herramientas de un proveedor dado de baja.
 */
public record UsuarioDesactivado(Long usuarioId, LocalDateTime ocurridoEn) implements DomainEvent {
}
