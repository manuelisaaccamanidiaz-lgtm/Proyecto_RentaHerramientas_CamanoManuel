package com.rentaherramientas.domain.billing.port.repository;

import com.rentaherramientas.domain.billing.model.aggregate.Pago;
import com.rentaherramientas.domain.billing.model.valueobject.ReferenciaPago;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida del agregado Pago.
 */
public interface PagoRepository {

    Pago guardar(Pago pago);

    Optional<Pago> buscarPorId(Long id);

    Optional<Pago> buscarPorReferencia(ReferenciaPago referencia);

    List<Pago> buscarPorReservaId(Long reservaId);
}
