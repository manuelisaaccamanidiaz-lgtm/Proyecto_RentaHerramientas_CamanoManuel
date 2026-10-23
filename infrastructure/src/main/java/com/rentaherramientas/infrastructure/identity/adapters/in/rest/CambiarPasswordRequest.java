package com.rentaherramientas.infrastructure.identity.adapters.in.rest;

import com.rentaherramientas.application.identity.command.CambiarPasswordCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de PATCH /api/usuarios/me/password. La nueva sigue las mismas
 * longitudes que el registro (8 a 72: BCrypt ignora lo que sobra); que la
 * actual sea correcta y la nueva distinta lo verifica el caso de uso.
 */
public record CambiarPasswordRequest(
        @Schema(description = "Contraseña vigente del usuario", example = "Clave123*", format = "password")
        @NotBlank @Size(max = 72) String passwordActual,
        @Schema(description = "Contraseña nueva de 8 a 72 caracteres", example = "NuevaClave456*", format = "password")
        @NotBlank @Size(min = 8, max = 72) String passwordNueva) {

    /** Convierte el cuerpo HTTP en el command de aplicación. */
    public CambiarPasswordCommand toCommand(Long usuarioId) {
        return new CambiarPasswordCommand(usuarioId, passwordActual, passwordNueva);
    }

    /** Oculta las contraseñas para que no aparezcan en logs. */
    @Override
    public String toString() {
        return "CambiarPasswordRequest[passwordActual=****, passwordNueva=****]";
    }
}
