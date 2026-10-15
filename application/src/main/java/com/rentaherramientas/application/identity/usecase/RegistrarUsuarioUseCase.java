package com.rentaherramientas.application.identity.usecase;

import com.rentaherramientas.application.identity.command.RegistrarUsuarioCommand;
import com.rentaherramientas.application.identity.dto.UsuarioResponse;

/**
 * Registro público de usuarios (solo CLIENTE o PROVEEDOR).
 */
public interface RegistrarUsuarioUseCase {

    UsuarioResponse registrar(RegistrarUsuarioCommand command);
}
