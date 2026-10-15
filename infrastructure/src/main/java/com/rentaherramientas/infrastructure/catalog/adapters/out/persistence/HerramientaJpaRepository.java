package com.rentaherramientas.infrastructure.catalog.adapters.out.persistence;

import com.rentaherramientas.domain.catalog.model.valueobject.EstadoHerramienta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

/**
 * Repositorio Spring Data de herramientas, uso interno del adaptador.
 * Las dos consultas "cargarCon..." inicializan en lote las colecciones de las
 * herramientas de una página. Van separadas porque imágenes (List) y categorías
 * (Set) no pueden traerse con fetch join en la misma consulta sin producir un
 * producto cartesiano (y Hibernate rechaza dos "bags" a la vez).
 */
public interface HerramientaJpaRepository extends JpaRepository<HerramientaEntity, Long>,
        JpaSpecificationExecutor<HerramientaEntity> {

    List<HerramientaEntity> findByProveedorId(Long proveedorId);

    @Query("select h.id from HerramientaEntity h where h.proveedorId = :proveedorId order by h.id")
    List<Long> idsPorProveedor(@Param("proveedorId") Long proveedorId);

    List<HerramientaEntity> findByEstado(EstadoHerramienta estado);

    @Query("select distinct h from HerramientaEntity h left join fetch h.imagenes where h.id in :ids")
    List<HerramientaEntity> cargarConImagenes(@Param("ids") Collection<Long> ids);

    @Query("select distinct h from HerramientaEntity h left join fetch h.categoriaIds where h.id in :ids")
    List<HerramientaEntity> cargarConCategorias(@Param("ids") Collection<Long> ids);
}
