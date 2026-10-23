package com.rentaherramientas.application.identity.command;

/**
 * Intención de un usuario de cambiar SU contraseña. El id sale del JWT, nunca
 * del cuerpo; ambas contraseñas viajan en claro hasta el caso de uso.
 */
public record CambiarPasswordCommand(Long usuarioId, String passwordActual, String passwordNueva) {

    /** Oculta las contraseñas para que no aparezcan en logs. */
    @Override
    public String toString() {
        return "CambiarPasswordCommand[usuarioId=" + usuarioId + ", passwordActual=****, passwordNueva=****]";
    }
}
