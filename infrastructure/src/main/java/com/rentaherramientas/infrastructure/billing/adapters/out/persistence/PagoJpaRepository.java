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

    Optional<PagoEntity> findByReferencia(String referencia);

    List<PagoEntity> findByReservaIdOrderByFechaAsc(Long reservaId);

    boolean existsByReservaIdAndEstadoIn(Long reservaId, Collection<EstadoPago> estados);

    @Query("select distinct p.reservaId from PagoEntity p where p.reservaId in :reservaIds and p.estado = :estado")
    List<Long> reservasConEstado(@Param("reservaIds") Collection<Long> reservaIds,
                                 @Param("estado") EstadoPago estado);
}
