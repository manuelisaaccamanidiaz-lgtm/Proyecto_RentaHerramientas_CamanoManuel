package com.rentaherramientas.domain.billing.event;

import com.rentaherramientas.domain.shared.event.DomainEvent;

import java.time.LocalDateTime;

/**
 * Se emite al rechazar un pago para que rental pueda liberar o vencer la reserva.
 */
public record PagoRechazado(Long pagoId, Long reservaId, LocalDateTime ocurridoEn) implements DomainEvent {
}
