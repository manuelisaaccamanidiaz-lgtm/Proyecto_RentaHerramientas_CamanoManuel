package com.rentaherramientas.application.identity.usecase;

import com.rentaherramientas.application.identity.command.LoginCommand;
import com.rentaherramientas.application.identity.dto.TokenResponse;

/**
 * Login: valida credenciales y emite un token de acceso.
 */
public interface AutenticarUsuarioUseCase {

    TokenResponse autenticar(LoginCommand command);
}
