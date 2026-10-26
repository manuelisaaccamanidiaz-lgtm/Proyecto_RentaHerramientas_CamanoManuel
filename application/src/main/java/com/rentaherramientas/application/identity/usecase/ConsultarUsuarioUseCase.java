package com.rentaherramientas.application.identity.usecase;

import com.rentaherramientas.application.identity.dto.UsuarioResponse;
import com.rentaherramientas.application.shared.dto.PaginaResponse;
import com.rentaherramientas.domain.identity.model.valueobject.Rol;

/**
 * Consultas de usuarios: el propio perfil (/me) y el listado paginado para administración.
 */
public interface ConsultarUsuarioUseCase {

    /** Devuelve el usuario con ese id. */
    UsuarioResponse buscarPorId(Long id);

    /** rol null = todos los roles. */
    PaginaResponse<UsuarioResponse> listar(Rol rol, int pagina, int tamano);
}
