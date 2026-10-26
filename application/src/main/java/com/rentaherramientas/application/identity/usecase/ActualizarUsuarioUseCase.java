package com.rentaherramientas.application.identity.usecase;

import com.rentaherramientas.application.identity.command.ActualizarUsuarioCommand;
import com.rentaherramientas.application.identity.dto.UsuarioResponse;

/**
 * Actualiza nombre y datos de rol: lo usa el ADMIN sobre cualquier usuario y
 * cada usuario sobre su propio perfil (/me).
 */
public interface ActualizarUsuarioUseCase {

    /** Actualiza los datos del usuario indicado en el comando y devuelve el resultado. */
    UsuarioResponse actualizar(ActualizarUsuarioCommand command);
}
