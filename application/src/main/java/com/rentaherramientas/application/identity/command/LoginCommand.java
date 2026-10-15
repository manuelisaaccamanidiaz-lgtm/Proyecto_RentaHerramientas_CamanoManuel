package com.rentaherramientas.application.identity.command;

/**
 * Intención de iniciar sesión con email y password en claro.
 */
public record LoginCommand(String email, String password) {

    @Override
    public String toString() {
        return "LoginCommand[email=" + email + ", password=****]";
    }
}
