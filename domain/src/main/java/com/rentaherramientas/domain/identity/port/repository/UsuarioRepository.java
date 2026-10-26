package com.rentaherramientas.domain.identity.port.repository;

import com.rentaherramientas.domain.identity.model.aggregate.Usuario;
import com.rentaherramientas.domain.identity.model.valueobject.Email;
import com.rentaherramientas.domain.identity.model.valueobject.Rol;
import com.rentaherramientas.domain.shared.model.Pagina;
import com.rentaherramientas.domain.shared.model.SolicitudPagina;

import java.util.Optional;

/**
 * Puerto de salida del agregado Usuario. El dominio define qué necesita y la
 * infraestructura decide cómo (JPA); así el dominio no depende de ningún framework.
 */
public interface UsuarioRepository {

    /** Inserta o actualiza y devuelve el agregado con el id asignado por la BD. */
    Usuario guardar(Usuario usuario);

    /** Busca un usuario por id; vacío si no existe. */
    Optional<Usuario> buscarPorId(Long id);

    /** Busca un usuario por email (ya normalizado); se usa en el login. */
    Optional<Usuario> buscarPorEmail(Email email);

    /** Indica si ya hay un usuario con ese email; se usa para impedir duplicados al registrar. */
    boolean existePorEmail(Email email);

    /** Usuarios ordenados por id; si rol no es null, solo los de ese rol. */
    Pagina<Usuario> buscar(Rol rol, SolicitudPagina pagina);
}
