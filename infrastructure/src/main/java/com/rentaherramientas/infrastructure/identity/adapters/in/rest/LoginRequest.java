package com.rentaherramientas.infrastructure.identity.adapters.in.rest;

import com.rentaherramientas.application.identity.command.LoginCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de POST /api/auth/login.
 */
public record LoginRequest(
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(max = 72) String password) {

    /** Convierte el cuerpo HTTP en el command de aplicación. */
    public LoginCommand toCommand() {
        return new LoginCommand(email, password);
    }

    /** Oculta la contraseña para que no aparezca en logs. */
    @Override
    public String toString() {
        return "LoginRequest[email=" + email + ", password=****]";
    }
}
