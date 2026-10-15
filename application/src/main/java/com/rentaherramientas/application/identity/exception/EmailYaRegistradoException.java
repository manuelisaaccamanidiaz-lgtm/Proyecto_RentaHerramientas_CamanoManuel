package com.rentaherramientas.application.identity.exception;

/**
 * Se lanza al registrar un usuario con un email que ya pertenece a otro.
 */
public class EmailYaRegistradoException extends RuntimeException {

    public EmailYaRegistradoException(String email) {
        super("El email " + email + " ya está registrado");
    }
}
