package com.rentaherramientas.application.identity.usecase;

import com.rentaherramientas.application.identity.dto.UsuarioResponse;
import com.rentaherramientas.application.identity.exception.UsuarioNoEncontradoException;
import com.rentaherramientas.domain.identity.port.repository.UsuarioRepository;

import java.util.List;

/**
 * Consultas de solo lectura sobre usuarios; siempre devuelve DTOs, nunca el agregado.
 */
public class ConsultarUsuarioService implements ConsultarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;

    public ConsultarUsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UsuarioResponse buscarPorId(Long id) {
        return usuarioRepository.buscarPorId(id)
                .map(UsuarioResponse::desde)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));
    }

    @Override
    public List<UsuarioResponse> listarTodos() {
        return usuarioRepository.buscarTodos().stream()
                .map(UsuarioResponse::desde)
                .toList();
    }
}
