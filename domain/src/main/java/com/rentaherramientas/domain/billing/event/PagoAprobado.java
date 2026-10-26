package com.rentaherramientas.domain.billing.event;

import com.rentaherramientas.domain.shared.event.DomainEvent;
import com.rentaherramientas.domain.shared.model.valueobject.Dinero;

import java.time.LocalDateTime;

/**
 * Se emite al aprobar un pago; dispara la emisión de la factura y permite a rental
 * confirmar la reserva sin que billing conozca el agregado Reserva.
 */
public record PagoAprobado(Long pagoId, Long reservaId, Dinero monto, LocalDateTime ocurridoEn)
        implements DomainEvent {
}
