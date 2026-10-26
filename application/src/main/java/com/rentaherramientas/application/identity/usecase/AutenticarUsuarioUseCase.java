package com.rentaherramientas.application.identity.usecase;

import com.rentaherramientas.application.identity.command.LoginCommand;
import com.rentaherramientas.application.identity.dto.TokenResponse;

/**
 * Login: valida credenciales y emite un token de acceso.
 */
public interface AutenticarUsuarioUseCase {

    /** Autentica con email y contraseña y devuelve el token de acceso. */
    TokenResponse autenticar(LoginCommand command);
}
