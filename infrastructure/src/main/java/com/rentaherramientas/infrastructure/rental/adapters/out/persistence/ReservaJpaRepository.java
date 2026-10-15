package com.rentaherramientas.infrastructure.rental.adapters.out.persistence;

import com.rentaherramientas.domain.rental.model.valueobject.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * Repositorio Spring Data de reservas, uso interno del adaptador.
 */
public interface ReservaJpaRepository extends JpaRepository<ReservaEntity, Long> {

    List<ReservaEntity> findByClienteIdOrderByCreadoEnDesc(Long clienteId);

    List<ReservaEntity> findByHerramientaIdOrderByFechaInicioAsc(Long herramientaId);

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
}
