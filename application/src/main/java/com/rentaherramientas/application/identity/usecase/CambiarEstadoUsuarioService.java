package com.rentaherramientas.application.identity.usecase;

import com.rentaherramientas.application.identity.dto.UsuarioResponse;
import com.rentaherramientas.application.identity.exception.AutoDesactivacionException;
import com.rentaherramientas.application.identity.exception.UsuarioNoEncontradoException;
import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.domain.identity.model.aggregate.Usuario;
import com.rentaherramientas.domain.identity.port.repository.UsuarioRepository;

/**
 * Activa o desactiva un usuario. Ambas operaciones son idempotentes en el
 * agregado. Un ADMIN no puede desactivarse a sí mismo.
 */
public class CambiarEstadoUsuarioService implements CambiarEstadoUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;

    /** Recibe sus dependencias por constructor; el bean lo crea IdentityBeansConfig. */
    public CambiarEstadoUsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /** Desactiva un usuario; un administrador no puede desactivarse a sí mismo. */
    @Override
    public UsuarioResponse desactivar(Long usuarioId, Solicitante solicitante) {
        if (usuarioId.equals(solicitante.id())) {
            throw new AutoDesactivacionException();
        }
        Usuario usuario = buscar(usuarioId);
        usuario.desactivar();
        return UsuarioResponse.desde(usuarioRepository.guardar(usuario));
    }

    /** Reactiva un usuario. */
    @Override
    public UsuarioResponse activar(Long usuarioId) {
        Usuario usuario = buscar(usuarioId);
        usuario.activar();
        return UsuarioResponse.desde(usuarioRepository.guardar(usuario));
    }

    /** Carga el usuario o lanza UsuarioNoEncontradoException (404). */
    private Usuario buscar(Long usuarioId) {
        return usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontradoException(usuarioId));
    }
}
