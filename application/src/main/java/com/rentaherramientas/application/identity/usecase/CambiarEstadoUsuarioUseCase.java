package com.rentaherramientas.application.identity.usecase;

import com.rentaherramientas.application.identity.dto.UsuarioResponse;
import com.rentaherramientas.application.shared.command.Solicitante;

/**
 * Activa o desactiva usuarios (solo ADMIN). Desactivar es el "eliminar" del
 * sistema: borrado lógico, porque herramientas y reservas referencian al usuario.
 */
public interface CambiarEstadoUsuarioUseCase {

    UsuarioResponse desactivar(Long usuarioId, Solicitante solicitante);

    UsuarioResponse activar(Long usuarioId);
}
