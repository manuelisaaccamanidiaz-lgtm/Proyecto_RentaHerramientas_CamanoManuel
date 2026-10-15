package com.rentaherramientas.application.identity.usecase;

import com.rentaherramientas.application.identity.dto.UsuarioResponse;

import java.util.List;

/**
 * Consultas de usuarios: el propio perfil (/me) y el listado para administración.
 */
public interface ConsultarUsuarioUseCase {

    UsuarioResponse buscarPorId(Long id);

    List<UsuarioResponse> listarTodos();
}
