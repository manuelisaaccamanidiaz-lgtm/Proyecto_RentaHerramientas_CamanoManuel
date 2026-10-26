package com.rentaherramientas.domain.catalog.event;

import com.rentaherramientas.domain.catalog.model.valueobject.EstadoHerramienta;
import com.rentaherramientas.domain.shared.event.DomainEvent;

import java.time.LocalDateTime;

/**
 * Se emite en cada cambio de estado de una herramienta. Rental puede escucharlo
 * para, por ejemplo, rechazar reservas pendientes de una herramienta que se dañó.
 */
public record HerramientaEstadoCambiado(Long herramientaId,
                                        EstadoHerramienta estadoAnterior,
                                        EstadoHerramienta estadoNuevo,
                                        LocalDateTime ocurridoEn) implements DomainEvent {
}
