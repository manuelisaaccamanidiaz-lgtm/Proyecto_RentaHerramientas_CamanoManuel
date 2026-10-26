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

    /** Busca la factura por número. */
    Optional<FacturaEntity> findByNumero(String numero);

    /** Indica si el pago ya tiene factura. */
    boolean existsByPagoId(Long pagoId);

    /** Busca la factura de un pago. */
    Optional<FacturaEntity> findByPagoId(Long pagoId);

    /**
     * Facturas de los pagos de esas reservas; pagos y facturas son del mismo BC.
     *
     * TÉCNICA AVANZADA (JPQL): subconsulta con `IN`
     *   Qué hace: filtra las facturas cuyo pago pertenece a alguna de las reservas indicadas, sin necesitar
     *     una relación JPA entre las entidades. Se declara una `countQuery` propia para la paginación.
     */
    @Query(value = """
            select f from FacturaEntity f
            where f.pagoId in (select p.id from PagoEntity p where p.reservaId in :reservaIds)
            """,
            countQuery = """
            select count(f) from FacturaEntity f
            where f.pagoId in (select p.id from PagoEntity p where p.reservaId in :reservaIds)
            """)
    Page<FacturaEntity> buscarPorReservas(@Param("reservaIds") Collection<Long> reservaIds, Pageable pageable);

    /** Inicializa en una consulta las líneas de las facturas indicadas (evita el N+1). */
    @Query("select distinct f from FacturaEntity f left join fetch f.lineas where f.id in :ids")
    List<FacturaEntity> cargarConLineas(@Param("ids") Collection<Long> ids);

    /** Facturas de los pagos indicados, con sus líneas ya cargadas. */
    @Query("select distinct f from FacturaEntity f left join fetch f.lineas where f.pagoId in :pagoIds")
    List<FacturaEntity> buscarPorPagosConLineas(@Param("pagoIds") Collection<Long> pagoIds);
}
