package com.rentaherramientas.application.billing.port;

import com.rentaherramientas.application.billing.dto.FacturaResponse;
import com.rentaherramientas.domain.billing.model.valueobject.MetodoPago;

/**
 * Todo lo que se imprime en el PDF de una factura.
 */
public record DocumentoFactura(FacturaResponse factura, Tercero cliente, Tercero proveedor,
                               String referenciaPago, MetodoPago metodoPago) {
}
