package com.rentaherramientas.infrastructure.catalog.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

/**
 * Repositorio Spring Data de categorías, uso interno del adaptador.
 */
public interface CategoriaJpaRepository extends JpaRepository<CategoriaEntity, Long> {

    /** Indica si existe una categoría con ese nombre exacto. */
    boolean existsByNombre(String nombre);

    /** Busca la categoría por nombre exacto. */
    Optional<CategoriaEntity> findByNombre(String nombre);

    /**
     * Cuenta cuántos de los ids indicados existen (`WHERE id IN (...)`).
     *
     * TÉCNICA AVANZADA (Spring Data): `countBy...In`
     *   Qué hace: el sufijo `In` genera un `IN (...)` con la colección, y `countBy` un `SELECT COUNT`. Así se
     *     comprueba que existen N ids con una sola consulta.
     */
    long countByIdIn(Collection<Long> ids);

    /**
     * true si alguna herramienta usa la categoría. Consulta nativa porque la tabla
     * de unión herramienta_categorias no tiene entidad propia; `exists` se detiene
     * en la primera fila (usa ix_herramienta_categorias_categoria_id).
     */
    @Query(value = "select exists(select 1 from herramienta_categorias where categoria_id = :id)",
            nativeQuery = true)
    boolean estaEnUso(@Param("id") Long id);
}
