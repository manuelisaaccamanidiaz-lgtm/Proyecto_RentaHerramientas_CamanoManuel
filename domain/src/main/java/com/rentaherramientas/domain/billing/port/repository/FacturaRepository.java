package com.rentaherramientas.domain.billing.port.repository;

import com.rentaherramientas.domain.billing.model.aggregate.Factura;
import com.rentaherramientas.domain.billing.model.valueobject.NumeroFactura;

import java.util.Optional;

/**
 * Puerto de salida del agregado Factura.
 */
public interface FacturaRepository {

    Factura guardar(Factura factura);

    Optional<Factura> buscarPorId(Long id);

    Optional<Factura> buscarPorNumero(NumeroFactura numero);

    /** Un pago solo puede facturarse una vez (uq_facturas_pago). */
    boolean existePorPagoId(Long pagoId);
}
