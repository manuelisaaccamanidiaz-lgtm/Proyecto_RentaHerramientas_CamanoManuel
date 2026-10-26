package com.rentaherramientas.infrastructure.identity.adapters.in.rest;

import com.rentaherramientas.application.identity.command.LoginCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de POST /api/auth/login.
 */
public record LoginRequest(
        @Schema(description = "Email con el que se registró el usuario", example = "andres.rueda@gmail.com")
        @NotBlank @Email @Size(max = 150) String email,
        @Schema(description = "Contraseña del usuario", example = "Clave123*", format = "password")
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
