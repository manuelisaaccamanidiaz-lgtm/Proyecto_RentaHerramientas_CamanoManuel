package com.rentaherramientas.domain.billing.port.repository;

import com.rentaherramientas.domain.billing.model.aggregate.Factura;
import com.rentaherramientas.domain.billing.model.valueobject.NumeroFactura;
import com.rentaherramientas.domain.shared.model.Pagina;
import com.rentaherramientas.domain.shared.model.SolicitudPagina;

import java.util.Collection;
import java.util.List;
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

    Optional<Factura> buscarPorPagoId(Long pagoId);

    /** Facturas (con sus líneas) de los pagos dados, en una sola consulta. */
    List<Factura> buscarPorPagoIds(Collection<Long> pagoIds);

    /**
     * Facturas paginadas, de la más reciente a la más antigua. Si reservaIds no es
     * null, solo las de pagos de esas reservas (vacía = ninguna).
     */
    Pagina<Factura> buscar(Collection<Long> reservaIds, SolicitudPagina pagina);
}
