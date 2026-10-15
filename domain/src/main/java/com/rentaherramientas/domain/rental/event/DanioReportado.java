package com.rentaherramientas.domain.rental.event;

import java.time.LocalDateTime;

import com.rentaherramientas.domain.shared.event.DomainEvent;
import com.rentaherramientas.domain.shared.model.valueobject.Dinero;

/**
 * Se emite al reportar un daño tras la devolución. Catalog puede escucharlo para
 * marcar la herramienta como DANADA y billing para cobrar el costo estimado.
 */
public record DanioReportado(Long reservaId,
                             Long herramientaId,
                             Dinero costoEstimado,
                             LocalDateTime ocurridoEn) implements DomainEvent {
}
