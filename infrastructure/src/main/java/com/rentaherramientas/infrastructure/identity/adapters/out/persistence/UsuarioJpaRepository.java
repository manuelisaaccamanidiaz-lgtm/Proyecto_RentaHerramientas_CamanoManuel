package com.rentaherramientas.infrastructure.identity.adapters.out.persistence;

import com.rentaherramientas.domain.identity.model.valueobject.Rol;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repositorio Spring Data sobre UsuarioEntity. Es un detalle técnico interno del
 * adaptador: el resto del sistema solo ve el puerto UsuarioRepository del dominio.
 * Los listados traen proveedor y cliente con fetch join: son relaciones 1:1 del
 * lado inverso, que Hibernate cargaría con una consulta extra por usuario (N+1).
 * Al ser relaciones a uno, el join no multiplica filas y la paginación sigue en la BD.
 *
 * TÉCNICA AVANZADA (Spring Data JPA): consultas derivadas del nombre
 *   Qué hace: Spring genera la implementación de `findByEmail` y `existsByEmail` leyendo el nombre del
 *     método, sin escribir SQL. `existsBy...` hace un `SELECT` que solo comprueba si hay filas.
 */
public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long> {

    /** Busca el usuario por email exacto. */
    Optional<UsuarioEntity> findByEmail(String email);

    /** Indica si ya existe un usuario con ese email. */
    boolean existsByEmail(String email);

    /**
     * Todos los usuarios paginados, con sus datos de proveedor y cliente cargados en la misma consulta.
     *
     * TÉCNICA AVANZADA (JPQL): `left join fetch` + `countQuery`
     *   Qué hace: `join fetch` carga las relaciones en la misma consulta. Con paginación hace falta una
     *     `countQuery` propia, porque Spring no puede derivar el `COUNT` de una consulta con fetch.
     */
    @Query(value = "select u from UsuarioEntity u left join fetch u.proveedor left join fetch u.cliente",
            countQuery = "select count(u) from UsuarioEntity u")
    Page<UsuarioEntity> buscarTodos(Pageable pageable);

    /**
     * Usuarios de un rol, paginados y con sus datos de rol cargados.
     *
     * Nota: `@Param("rol")` enlaza el parámetro del método con `:rol` en la consulta.
     */
    @Query(value = "select u from UsuarioEntity u left join fetch u.proveedor left join fetch u.cliente"
            + " where u.rol = :rol",
            countQuery = "select count(u) from UsuarioEntity u where u.rol = :rol")
    Page<UsuarioEntity> buscarPorRol(@Param("rol") Rol rol, Pageable pageable);
}
