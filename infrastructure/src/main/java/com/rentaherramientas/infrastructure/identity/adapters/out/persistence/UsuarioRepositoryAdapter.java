package com.rentaherramientas.infrastructure.identity.adapters.out.persistence;

import com.rentaherramientas.domain.identity.model.aggregate.Usuario;
import com.rentaherramientas.domain.identity.model.valueobject.Email;
import com.rentaherramientas.domain.identity.model.valueobject.Rol;
import com.rentaherramientas.domain.identity.port.repository.UsuarioRepository;
import com.rentaherramientas.domain.shared.model.Pagina;
import com.rentaherramientas.domain.shared.model.SolicitudPagina;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Adaptador de salida: implementa el puerto del dominio usando Spring Data JPA.
 * Las lecturas son transaccionales para que el mapper pueda recorrer relaciones
 * perezosas antes de cerrar la sesión de Hibernate.
 *
 * TÉCNICA AVANZADA (Spring): `@Transactional(readOnly = true)`
 *   Qué hace: indica a Hibernate que no habrá escrituras, así se salta la detección de cambios (dirty
 *     checking) y la consulta es más ligera. `@Repository` además traduce las excepciones de la BD a la
 *     jerarquía `DataAccessException` de Spring.
 */
@Repository
public class UsuarioRepositoryAdapter implements UsuarioRepository {

    private final UsuarioJpaRepository jpaRepository;
    private final UsuarioPersistenceMapper mapper;

    /** Recibe el repositorio de Spring Data y el mapper por constructor. */
    public UsuarioRepositoryAdapter(UsuarioJpaRepository jpaRepository, UsuarioPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    /** Guarda el usuario (INSERT si no tiene id, UPDATE si lo tiene) y devuelve el agregado actualizado. */
    @Override
    @Transactional
    public Usuario guardar(Usuario usuario) {
        UsuarioEntity guardado = jpaRepository.save(mapper.toEntity(usuario));
        return mapper.toDomain(guardado);
    }

    /** Busca por id y convierte la entidad en agregado. */
    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    /** Busca por el email normalizado. */
    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorEmail(Email email) {
        return jpaRepository.findByEmail(email.valor()).map(mapper::toDomain);
    }

    /** Indica si el email ya está registrado. */
    @Override
    @Transactional(readOnly = true)
    public boolean existePorEmail(Email email) {
        return jpaRepository.existsByEmail(email.valor());
    }

    /**
     * Lista usuarios ordenados por id; filtra por rol si se indica.
     *
     * Nota: `PageRequest.of(numero, tamano, Sort.by("id"))` traduce la paginación del dominio al `Pageable`
     *   de Spring Data, que genera `LIMIT/OFFSET` en el SQL.
     */
    @Override
    @Transactional(readOnly = true)
    public Pagina<Usuario> buscar(Rol rol, SolicitudPagina pagina) {
        PageRequest pageRequest = PageRequest.of(pagina.numero(), pagina.tamano(), Sort.by("id"));
        Page<UsuarioEntity> resultado = rol == null
                ? jpaRepository.buscarTodos(pageRequest)
                : jpaRepository.buscarPorRol(rol, pageRequest);
        return new Pagina<>(resultado.getContent().stream().map(mapper::toDomain).toList(),
                pagina.numero(), pagina.tamano(), resultado.getTotalElements());
    }
}
