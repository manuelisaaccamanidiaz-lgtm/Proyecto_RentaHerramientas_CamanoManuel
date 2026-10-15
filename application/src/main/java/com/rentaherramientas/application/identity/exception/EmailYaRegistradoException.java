package com.rentaherramientas.application.identity.exception;

import com.rentaherramientas.application.shared.exception.ConflictoException;

/**
 * Se lanza al registrar un usuario con un email que ya pertenece a otro.
 */
public class EmailYaRegistradoException extends ConflictoException {

    public EmailYaRegistradoException(String email) {
        super("El email " + email + " ya está registrado");
    }
}
