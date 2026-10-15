package com.rentaherramientas.infrastructure.catalog.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

/**
 * Repositorio Spring Data de categorías, uso interno del adaptador.
 */
public interface CategoriaJpaRepository extends JpaRepository<CategoriaEntity, Long> {

    boolean existsByNombre(String nombre);

    Optional<CategoriaEntity> findByNombre(String nombre);

    long countByIdIn(Collection<Long> ids);
}
