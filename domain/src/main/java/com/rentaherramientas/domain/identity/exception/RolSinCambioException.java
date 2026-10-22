package com.rentaherramientas.domain.identity.exception;

import com.rentaherramientas.domain.identity.model.valueobject.Rol;
import com.rentaherramientas.domain.shared.exception.DomainException;

/**
 * Regla: cambiar el rol exige un rol distinto del actual. Reasignar el mismo rol
 * no es un cambio y probablemente indica un error de quien hace la petición.
 */
public class RolSinCambioException extends DomainException {

    /** Crea la excepción indicando el usuario y el rol que ya tiene. */
    public RolSinCambioException(Long usuarioId, Rol rol) {
        super("El usuario " + usuarioId + " ya tiene el rol " + rol);
    }
}
