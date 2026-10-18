package com.rentaherramientas.application.shared.exception;

/**
 * Se lanza cuando el usuario autenticado intenta operar sobre un recurso que no
 * le pertenece (p. ej. un PROVEEDOR editando la herramienta de otro). La
 * seguridad por URL solo conoce roles; la propiedad la decide el caso de uso.
 */
public class AccesoDenegadoException extends RuntimeException {

    /** Crea la excepción con el motivo del rechazo (se traduce a HTTP 403). */
    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
