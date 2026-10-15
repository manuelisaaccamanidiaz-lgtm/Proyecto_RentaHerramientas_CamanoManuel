package com.rentaherramientas.application.identity.exception;

import com.rentaherramientas.application.shared.exception.RecursoNoEncontradoException;

/**
 * Se lanza cuando se consulta un usuario por id y no existe.
 */
public class UsuarioNoEncontradoException extends RecursoNoEncontradoException {

    public UsuarioNoEncontradoException(Long id) {
        super("No existe el usuario " + id);
    }
}
