package com.rentaherramientas.application.shared.exception;

/**
 * Se lanza cuando el usuario autenticado intenta operar sobre un recurso que no
 * le pertenece (p. ej. un PROVEEDOR editando la herramienta de otro). La
 * seguridad por URL solo conoce roles; la propiedad la decide el caso de uso.
 */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
