package com.rentaherramientas.infrastructure.rental.adapters.out.persistence;

import com.rentaherramientas.domain.rental.model.valueobject.EstadoReserva;
import com.rentaherramientas.domain.rental.model.valueobject.RangoFechas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * Repositorio Spring Data de reservas, uso interno del adaptador.
 */
public interface ReservaJpaRepository extends JpaRepository<ReservaEntity, Long>,
        JpaSpecificationExecutor<ReservaEntity> {

    List<ReservaEntity> findByClienteIdOrderByCreadoEnDesc(Long clienteId);

    List<ReservaEntity> findByHerramientaIdOrderByFechaInicioAsc(Long herramientaId);

    @Query("select r.id from ReservaEntity r where r.clienteId = :clienteId order by r.id")
    List<Long> idsPorCliente(@Param("clienteId") Long clienteId);

    @Query("select r.id from ReservaEntity r where r.herramientaId in :herramientaIds order by r.id")
    List<Long> idsPorHerramientas(@Param("herramientaIds") Collection<Long> herramientaIds);

    /** Rangos inclusivos: se cruzan si cada uno empieza antes (o el mismo día) de que termine el otro. */
    @Query("""
            select count(r) > 0 from ReservaEntity r
            where r.herramientaId = :herramientaId
              and r.estado in :estados
              and r.fechaInicio <= :fin
              and r.fechaFin >= :inicio
            """)
    boolean existeSolapamiento(@Param("herramientaId") Long herramientaId,
                               @Param("inicio") LocalDate inicio,
                               @Param("fin") LocalDate fin,
                               @Param("estados") Collection<EstadoReserva> estados);

    /** Solo las fechas: el endpoint de disponibilidad es público y no debe cargar reservas completas. */
    @Query("""
            select new com.rentaherramientas.domain.rental.model.valueobject.RangoFechas(r.fechaInicio, r.fechaFin)
            from ReservaEntity r
            where r.herramientaId = :herramientaId
              and r.estado in :estados
              and r.fechaInicio <= :fin
              and r.fechaFin >= :inicio
            order by r.fechaInicio
            """)
    List<RangoFechas> rangosOcupados(@Param("herramientaId") Long herramientaId,
                                     @Param("inicio") LocalDate inicio,
                                     @Param("fin") LocalDate fin,
                                     @Param("estados") Collection<EstadoReserva> estados);

    /** Inicializa en lote los reportes de las reservas de una página (evita el N+1). */
    @Query("select distinct r from ReservaEntity r left join fetch r.reportes where r.id in :ids")
    List<ReservaEntity> cargarConReportes(@Param("ids") Collection<Long> ids);

    /** Sin paginación, así que los reportes pueden venir en la misma consulta con fetch join. */
    @Query("""
            select distinct r from ReservaEntity r left join fetch r.reportes
            where (r.estado = :pendiente and r.fechaInicio < :hoy)
               or (r.estado = :aceptada and r.fechaFin < :hoy)
            """)
    List<ReservaEntity> buscarVencibles(@Param("hoy") LocalDate hoy,
                                        @Param("pendiente") EstadoReserva pendiente,
                                        @Param("aceptada") EstadoReserva aceptada);
}
