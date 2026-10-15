package com.rentaherramientas.domain.rental.event;

import java.time.LocalDateTime;

import com.rentaherramientas.domain.rental.model.valueobject.RangoFechas;
import com.rentaherramientas.domain.shared.event.DomainEvent;

/**
 * Se emite al registrar una reserva nueva (p. ej. para avisar al proveedor).
 * Aún no lleva id de reserva: se genera al persistir.
 */
public record ReservaSolicitada(Long herramientaId,
                                Long clienteId,
                                RangoFechas rango,
                                LocalDateTime ocurridoEn) implements DomainEvent {
}