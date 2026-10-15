package com.rentaherramientas.infrastructure.identity.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio Spring Data sobre UsuarioEntity. Es un detalle técnico interno del
 * adaptador: el resto del sistema solo ve el puerto UsuarioRepository del dominio.
 */
public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long> {

    Optional<UsuarioEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
