package com.rentaherramientas.infrastructure.billing.adapters.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data de facturas, uso interno del adaptador. Las consultas
 * paginadas no traen las líneas (un fetch join con paginación obliga a paginar
 * en memoria); se cargan después, en lote, con cargarConLineas.
 */
public interface FacturaJpaRepository extends JpaRepository<FacturaEntity, Long> {

    Optional<FacturaEntity> findByNumero(String numero);

    boolean existsByPagoId(Long pagoId);

    Optional<FacturaEntity> findByPagoId(Long pagoId);

    /** Facturas de los pagos de esas reservas; pagos y facturas son del mismo BC. */
    @Query(value = """
            select f from FacturaEntity f
            where f.pagoId in (select p.id from PagoEntity p where p.reservaId in :reservaIds)
            """,
            countQuery = """
            select count(f) from FacturaEntity f
            where f.pagoId in (select p.id from PagoEntity p where p.reservaId in :reservaIds)
            """)
    Page<FacturaEntity> buscarPorReservas(@Param("reservaIds") Collection<Long> reservaIds, Pageable pageable);

    @Query("select distinct f from FacturaEntity f left join fetch f.lineas where f.id in :ids")
    List<FacturaEntity> cargarConLineas(@Param("ids") Collection<Long> ids);

    @Query("select distinct f from FacturaEntity f left join fetch f.lineas where f.pagoId in :pagoIds")
    List<FacturaEntity> buscarPorPagosConLineas(@Param("pagoIds") Collection<Long> pagoIds);
}
