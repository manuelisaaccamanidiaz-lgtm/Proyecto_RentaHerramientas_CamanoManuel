package com.rentaherramientas.infrastructure.catalog.adapters.out.persistence;

import com.rentaherramientas.domain.catalog.model.valueobject.EstadoHerramienta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio Spring Data de herramientas, uso interno del adaptador.
 */
public interface HerramientaJpaRepository extends JpaRepository<HerramientaEntity, Long> {

    List<HerramientaEntity> findByProveedorId(Long proveedorId);

    List<HerramientaEntity> findByEstado(EstadoHerramienta estado);
}
