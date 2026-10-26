package com.rentaherramientas.domain.rental.event;

import java.time.LocalDateTime;

import com.rentaherramientas.domain.rental.model.valueobject.RangoFechas;
import com.rentaherramientas.domain.shared.event.DomainEvent;
import com.rentaherramientas.domain.shared.model.valueobject.Dinero;

/**
 * Se emite cuando el cliente cambia las fechas de su reserva PENDIENTE (p. ej.
 * para avisar al proveedor de las nuevas fechas y el nuevo total).
 */
public record ReservaReprogramada(Long reservaId,
                                  Long herramientaId,
                                  Long clienteId,
                                  RangoFechas rangoAnterior,
                                  RangoFechas rangoNuevo,
                                  Dinero precioTotal,
                                  LocalDateTime ocurridoEn) implements DomainEvent {
}
