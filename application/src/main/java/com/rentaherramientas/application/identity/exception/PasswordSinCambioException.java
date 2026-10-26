package com.rentaherramientas.application.identity.exception;

/**
 * Se lanza cuando la contraseña nueva es igual a la actual. La regla vive en
 * aplicación y no en el agregado porque el dominio solo ve hashes, y BCrypt
 * genera un hash distinto para la misma contraseña (sal aleatoria).
 */
public class PasswordSinCambioException extends RuntimeException {

    /** Crea la excepción con un mensaje genérico. */
    public PasswordSinCambioException() {
        super("La contraseña nueva debe ser distinta de la actual");
    }
}
