package com.rentaherramientas.application.shared.exception;

/**
 * Base de las excepciones "no existe el recurso pedido" de todos los BC. El
 * adaptador REST las traduce a 404 sin conocer cada subclase.
 */
public abstract class RecursoNoEncontradoException extends RuntimeException {

    protected RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
