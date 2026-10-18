package com.rentaherramientas.domain.identity.exception;

import com.rentaherramientas.domain.shared.exception.DomainException;

/**
 * Regla: los datos propios de un rol deben coincidir con el rol del usuario.
 * PROVEEDOR exige DatosProveedor, CLIENTE exige DatosCliente y ADMIN no admite
 * ninguno. La base de datos no puede expresar esta regla entre tres tablas
 * (usuarios, proveedores, clientes), así que la protege el agregado Usuario.
 */
public class DatosRolInconsistentesException extends DomainException {

    /** Crea la excepción indicando qué dato de rol sobra o falta. */
    public DatosRolInconsistentesException(String mensaje) {
        super(mensaje);
    }
}
