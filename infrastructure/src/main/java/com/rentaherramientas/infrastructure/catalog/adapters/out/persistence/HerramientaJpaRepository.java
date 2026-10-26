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
 *
 * TÉCNICA AVANZADA (Spring Data): `JpaSpecificationExecutor`
 *   Qué hace: añade métodos como `findAll(Specification, Pageable)` para construir consultas dinámicas con
 *     filtros opcionales (ver HerramientaSpecifications).
 */
public interface HerramientaJpaRepository extends JpaRepository<HerramientaEntity, Long>,
        JpaSpecificationExecutor<HerramientaEntity> {

    /** Todas las herramientas de un proveedor (consulta derivada del nombre). */
    List<HerramientaEntity> findByProveedorId(Long proveedorId);

    /**
     * Solo los ids de las herramientas de un proveedor.
     *
     * TÉCNICA AVANZADA (JPQL): proyección escalar
     *   Qué hace: seleccionar `h.id` en vez de `h` devuelve una lista de Long sin cargar entidades completas.
     */
    @Query("select h.id from HerramientaEntity h where h.proveedorId = :proveedorId order by h.id")
    List<Long> idsPorProveedor(@Param("proveedorId") Long proveedorId);

    /** Todas las herramientas en un estado (consulta derivada del nombre). */
    List<HerramientaEntity> findByEstado(EstadoHerramienta estado);

    /**
     * Inicializa en una consulta las imágenes de las herramientas indicadas.
     *
     * Nota: `distinct` evita que la herramienta aparezca repetida una vez por cada imagen. El resultado se
     *   ignora: lo que importa es que Hibernate rellena las entidades que ya tiene en memoria.
     */
    @Query("select distinct h from HerramientaEntity h left join fetch h.imagenes where h.id in :ids")
    List<HerramientaEntity> cargarConImagenes(@Param("ids") Collection<Long> ids);

    /** Inicializa en una consulta las categorías de las herramientas indicadas. */
    @Query("select distinct h from HerramientaEntity h left join fetch h.categoriaIds where h.id in :ids")
    List<HerramientaEntity> cargarConCategorias(@Param("ids") Collection<Long> ids);
}
