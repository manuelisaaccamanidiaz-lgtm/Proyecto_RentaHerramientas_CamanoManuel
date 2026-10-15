package com.rentaherramientas.domain.billing.port.repository;

import com.rentaherramientas.domain.billing.model.aggregate.Pago;
import com.rentaherramientas.domain.billing.model.valueobject.ReferenciaPago;
import com.rentaherramientas.domain.shared.model.Pagina;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Puerto de salida del agregado Pago.
 */
public interface PagoRepository {

    /**
     * Inserta o actualiza. Si la reserva ya tiene otro pago PENDIENTE o APROBADO
     * (carrera entre dos pagos simultáneos), lanza PagoDuplicadoException.
     */
    Pago guardar(Pago pago);

    Optional<Pago> buscarPorId(Long id);

    Optional<Pago> buscarPorReferencia(ReferenciaPago referencia);

    List<Pago> buscarPorReservaId(Long reservaId);

    /** true si la reserva tiene un pago PENDIENTE o APROBADO (un pago RECHAZADO no cuenta). */
    boolean existePagoVigente(Long reservaId);

    boolean existePagoAprobado(Long reservaId);

    /** De las reservas dadas, las que tienen un pago APROBADO. */
    Set<Long> reservasConPagoAprobado(Collection<Long> reservaIds);

    /** Búsqueda paginada con filtros opcionales, del más reciente al más antiguo. */
    Pagina<Pago> buscar(CriteriosBusquedaPago criterios);
}
