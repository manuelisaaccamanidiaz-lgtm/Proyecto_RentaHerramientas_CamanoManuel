package com.rentaherramientas.infrastructure.billing.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio Spring Data de facturas, uso interno del adaptador.
 */
public interface FacturaJpaRepository extends JpaRepository<FacturaEntity, Long> {

    Optional<FacturaEntity> findByNumero(String numero);

    boolean existsByPagoId(Long pagoId);
}
