package com.rentaherramientas.application.billing.dto;

import com.rentaherramientas.domain.billing.model.aggregate.Factura;
import com.rentaherramientas.domain.billing.model.aggregate.Pago;
import com.rentaherramientas.domain.billing.model.valueobject.EstadoPago;
import com.rentaherramientas.domain.billing.model.valueobject.MetodoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Vista de un pago. factura va informada cuando el pago está APROBADO (la
 * factura se emite en la misma operación que lo aprueba) y en null si no.
 */
public record PagoResponse(Long id, Long reservaId, BigDecimal monto, String moneda, MetodoPago metodo,
                           EstadoPago estado, String referencia, LocalDateTime fecha, FacturaResumen factura) {

    public record FacturaResumen(Long id, String numero, BigDecimal total) {
    }

    public static PagoResponse desde(Pago pago, Factura factura) {
        return new PagoResponse(
                pago.getId(),
                pago.getReservaId(),
                pago.getMonto().monto(),
                pago.getMonto().moneda(),
                pago.getMetodo(),
                pago.getEstado(),
                pago.getReferencia().valor(),
                pago.getFecha(),
                factura == null ? null
                        : new FacturaResumen(factura.getId(), factura.getNumero().valor(), factura.total().monto()));
    }
}
