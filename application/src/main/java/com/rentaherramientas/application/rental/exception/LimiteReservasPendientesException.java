package com.rentaherramientas.application.rental.exception;

import com.rentaherramientas.application.shared.exception.ConflictoException;

/**
 * Se lanza cuando el cliente ya tiene el máximo de reservas PENDIENTES (sin
 * respuesta del proveedor) y pide otra.
 */
public class LimiteReservasPendientesException extends ConflictoException {

    /** Crea la excepción indicando el límite configurado. */
    public LimiteReservasPendientesException(int maximo) {
        super("Ya tienes " + maximo + " reservas pendientes, que es el máximo permitido; espera a que el "
                + "proveedor responda alguna o cancela una antes de crear otra");
    }
}
