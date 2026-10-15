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
 */
public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long> {

    Optional<UsuarioEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query(value = "select u from UsuarioEntity u left join fetch u.proveedor left join fetch u.cliente",
            countQuery = "select count(u) from UsuarioEntity u")
    Page<UsuarioEntity> buscarTodos(Pageable pageable);

    @Query(value = "select u from UsuarioEntity u left join fetch u.proveedor left join fetch u.cliente"
            + " where u.rol = :rol",
            countQuery = "select count(u) from UsuarioEntity u where u.rol = :rol")
    Page<UsuarioEntity> buscarPorRol(@Param("rol") Rol rol, Pageable pageable);
}
