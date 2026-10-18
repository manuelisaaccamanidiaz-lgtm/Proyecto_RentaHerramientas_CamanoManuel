package com.rentaherramientas.application.identity.command;

/**
 * Intención de iniciar sesión con email y password en claro.
 */
public record LoginCommand(String email, String password) {

    /** Oculta la contraseña para que no aparezca en logs. */
    @Override
    public String toString() {
        return "LoginCommand[email=" + email + ", password=****]";
    }
}
