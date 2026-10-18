package com.rentaherramientas.application.shared.exception;

/**
 * Base de las excepciones "no existe el recurso pedido" de todos los BC. El
 * adaptador REST las traduce a 404 sin conocer cada subclase.
 */
public abstract class RecursoNoEncontradoException extends RuntimeException {

    /** Constructor protegido: solo lo usan las subclases concretas (se traducen a HTTP 404). */
    protected RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
