package com.rentaherramientas.infrastructure.billing.adapters.out.persistence;

import com.rentaherramientas.domain.billing.model.valueobject.EstadoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data de pagos, uso interno del adaptador.
 */
public interface PagoJpaRepository extends JpaRepository<PagoEntity, Long>, JpaSpecificationExecutor<PagoEntity> {

    /** Busca el pago por su referencia. */
    Optional<PagoEntity> findByReferencia(String referencia);

    /** Pagos de una reserva, del más antiguo al más reciente. */
    List<PagoEntity> findByReservaIdOrderByFechaAsc(Long reservaId);

    /**
     * Indica si la reserva tiene algún pago en alguno de los estados indicados.
     *
     * TÉCNICA AVANZADA (Spring Data): varias condiciones en el nombre
     *   Qué hace: `existsBy + ReservaId + And + EstadoIn` genera `WHERE reserva_id = ? AND estado IN (...)`.
     */
    boolean existsByReservaIdAndEstadoIn(Long reservaId, Collection<EstadoPago> estados);

    /** De las reservas indicadas, devuelve las que tienen algún pago en el estado dado. */
    @Query("select distinct p.reservaId from PagoEntity p where p.reservaId in :reservaIds and p.estado = :estado")
    List<Long> reservasConEstado(@Param("reservaIds") Collection<Long> reservaIds,
                                 @Param("estado") EstadoPago estado);
}
