package com.rentaherramientas.application.identity.exception;

/**
 * Se lanza cuando el email no existe o el password no coincide. El mensaje es
 * deliberadamente el mismo en ambos casos para no revelar qué emails existen.
 */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Credenciales inválidas");
    }
}
