package com.rentaherramientas.domain.billing.event;

import com.rentaherramientas.domain.shared.event.DomainEvent;

import java.time.LocalDateTime;

/**
 * Se emite al emitir una factura (p. ej. para enviarla por correo al cliente).
 * Quien necesite la reserva la obtiene a través del pago.
 */
public record FacturaEmitida(String numero, Long pagoId, LocalDateTime ocurridoEn) implements DomainEvent {
}
