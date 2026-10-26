package com.rentaherramientas.infrastructure.catalog.adapters.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

/**
 * Repositorio Spring Data de favoritos, uso interno del adaptador.
 */
public interface FavoritoJpaRepository extends JpaRepository<FavoritoEntity, FavoritoEntity.Id> {

    /**
     * Inserta el favorito o no hace nada si ya existe; devuelve las filas insertadas (0 o 1).
     *
     * TÉCNICA AVANZADA (PostgreSQL): `INSERT ... ON CONFLICT DO NOTHING`
     *   Qué hace: si la fila choca con la PK, la base la descarta en vez de lanzar un error. Así dos PUT
     *     simultáneos del mismo favorito no terminan en un 500 por clave duplicada, cosa que sí podría pasar
     *     con "consultar si existe y luego insertar". Es SQL nativo porque JPQL no tiene ON CONFLICT.
     */
    @Modifying
    @Query(value = """
            INSERT INTO favoritos (cliente_id, herramienta_id, creado_en)
            VALUES (:clienteId, :herramientaId, :creadoEn)
            ON CONFLICT DO NOTHING
            """, nativeQuery = true)
    int insertarSiNoExiste(@Param("clienteId") Long clienteId, @Param("herramientaId") Long herramientaId,
                           @Param("creadoEn") LocalDateTime creadoEn);

    /** Borra el favorito; devuelve las filas borradas (0 si no existía). */
    @Modifying
    @Query("delete from FavoritoEntity f where f.id.clienteId = :clienteId and f.id.herramientaId = :herramientaId")
    int eliminar(@Param("clienteId") Long clienteId, @Param("herramientaId") Long herramientaId);

    /**
     * Página de ids de herramientas favoritas del cliente, la más reciente primero
     * (el id desempata dos marcas del mismo instante para que la paginación sea estable).
     */
    @Query(value = """
            select f.id.herramientaId from FavoritoEntity f where f.id.clienteId = :clienteId
            order by f.creadoEn desc, f.id.herramientaId desc
            """,
            countQuery = "select count(f) from FavoritoEntity f where f.id.clienteId = :clienteId")
    Page<Long> herramientaIdsDe(@Param("clienteId") Long clienteId, Pageable pageable);
}
