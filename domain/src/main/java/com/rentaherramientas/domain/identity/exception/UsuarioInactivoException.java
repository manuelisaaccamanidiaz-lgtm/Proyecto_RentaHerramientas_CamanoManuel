package com.rentaherramientas.domain.identity.exception;

import com.rentaherramientas.domain.shared.exception.DomainException;

/**
 * Regla: un usuario desactivado no puede modificar sus datos hasta ser reactivado.
 */
public class UsuarioInactivoException extends DomainException {

    public UsuarioInactivoException(Long usuarioId) {
        super("El usuario " + usuarioId + " está inactivo");
    }
}
