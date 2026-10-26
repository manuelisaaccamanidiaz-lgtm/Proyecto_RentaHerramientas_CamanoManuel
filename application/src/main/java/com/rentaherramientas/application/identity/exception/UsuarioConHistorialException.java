package com.rentaherramientas.application.identity.exception;

import com.rentaherramientas.application.shared.exception.ConflictoException;

/**
 * Se lanza al intentar cambiar el rol de un usuario que ya tiene historial con
 * su rol actual (reservas como cliente o herramientas como proveedor): esos
 * registros dejarían de apuntar a un usuario de ese rol. Se traduce a 409.
 */
public class UsuarioConHistorialException extends ConflictoException {

    /** Crea la excepción con el motivo concreto del rechazo. */
    public UsuarioConHistorialException(String mensaje) {
        super(mensaje);
    }
}
