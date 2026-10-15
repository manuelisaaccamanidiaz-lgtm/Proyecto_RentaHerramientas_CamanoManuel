package com.rentaherramientas.application.identity.exception;

/**
 * Se lanza cuando se consulta un usuario por id y no existe.
 */
public class UsuarioNoEncontradoException extends RuntimeException {

    public UsuarioNoEncontradoException(Long id) {
        super("No existe el usuario " + id);
    }
}
