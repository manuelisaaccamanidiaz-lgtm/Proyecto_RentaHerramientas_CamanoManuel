package com.rentaherramientas.infrastructure.billing.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data de pagos, uso interno del adaptador.
 */
public interface PagoJpaRepository extends JpaRepository<PagoEntity, Long> {

    Optional<PagoEntity> findByReferencia(String referencia);

    List<PagoEntity> findByReservaIdOrderByFechaAsc(Long reservaId);
}
