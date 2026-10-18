package com.rentaherramientas.application.identity.exception;

import com.rentaherramientas.domain.identity.model.valueobject.Rol;

/**
 * Se lanza cuando el registro público intenta crear un usuario con un rol que
 * no admite (ADMIN). Los administradores solo se crean por configuración.
 */
public class RolNoPermitidoException extends RuntimeException {

    /** Crea la excepción indicando el rol rechazado. */
    public RolNoPermitidoException(Rol rol) {
        super("El registro público no permite el rol " + rol);
    }
}
