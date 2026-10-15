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
 */
@Repository
public class UsuarioRepositoryAdapter implements UsuarioRepository {

    private final UsuarioJpaRepository jpaRepository;
    private final UsuarioPersistenceMapper mapper;

    public UsuarioRepositoryAdapter(UsuarioJpaRepository jpaRepository, UsuarioPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Usuario guardar(Usuario usuario) {
        UsuarioEntity guardado = jpaRepository.save(mapper.toEntity(usuario));
        return mapper.toDomain(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorEmail(Email email) {
        return jpaRepository.findByEmail(email.valor()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorEmail(Email email) {
        return jpaRepository.existsByEmail(email.valor());
    }

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
