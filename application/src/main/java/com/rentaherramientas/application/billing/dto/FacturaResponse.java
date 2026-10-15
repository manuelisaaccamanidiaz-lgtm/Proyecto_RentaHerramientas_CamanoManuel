package com.rentaherramientas.application.billing.dto;

import com.rentaherramientas.domain.billing.model.aggregate.Factura;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Vista de una factura. Subtotales y total no existen en la BD: se calculan aquí
 * a partir de las líneas, igual que en el dominio.
 */
public record FacturaResponse(Long id, String numero, Long pagoId, LocalDateTime fechaEmision, String moneda,
                              List<LineaResponse> lineas, BigDecimal total) {

    public record LineaResponse(String descripcion, int cantidad, BigDecimal valorUnitario, BigDecimal subtotal) {
    }

    public static FacturaResponse desde(Factura factura) {
        return new FacturaResponse(
                factura.getId(),
                factura.getNumero().valor(),
                factura.getPagoId(),
                factura.getFechaEmision(),
                factura.getMoneda(),
                factura.getLineas().stream()
                        .map(linea -> new LineaResponse(linea.getDescripcion(), linea.getCantidad(),
                                linea.getValorUnitario().monto(), linea.subtotal().monto()))
                        .toList(),
                factura.total().monto());
    }
}
