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

    public CambiarEstadoUsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UsuarioResponse desactivar(Long usuarioId, Solicitante solicitante) {
        if (usuarioId.equals(solicitante.id())) {
            throw new AutoDesactivacionException();
        }
        Usuario usuario = buscar(usuarioId);
        usuario.desactivar();
        return UsuarioResponse.desde(usuarioRepository.guardar(usuario));
    }

    @Override
    public UsuarioResponse activar(Long usuarioId) {
        Usuario usuario = buscar(usuarioId);
        usuario.activar();
        return UsuarioResponse.desde(usuarioRepository.guardar(usuario));
    }

    private Usuario buscar(Long usuarioId) {
        return usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontradoException(usuarioId));
    }
}
