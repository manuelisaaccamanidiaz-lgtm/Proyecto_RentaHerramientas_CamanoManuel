package com.rentaherramientas.domain.identity.port.repository;

import com.rentaherramientas.domain.identity.model.aggregate.Usuario;
import com.rentaherramientas.domain.identity.model.valueobject.Email;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida del agregado Usuario. El dominio define qué necesita y la
 * infraestructura decide cómo (JPA); así el dominio no depende de ningún framework.
 */
public interface UsuarioRepository {

    /** Inserta o actualiza y devuelve el agregado con el id asignado por la BD. */
    Usuario guardar(Usuario usuario);

    Optional<Usuario> buscarPorId(Long id);

    Optional<Usuario> buscarPorEmail(Email email);

    boolean existePorEmail(Email email);

    /** Todos los usuarios ordenados por id. */
    List<Usuario> buscarTodos();
}
