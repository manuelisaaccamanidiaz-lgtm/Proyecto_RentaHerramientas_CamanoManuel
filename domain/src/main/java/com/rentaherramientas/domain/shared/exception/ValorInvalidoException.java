package com.rentaherramientas.domain.shared.exception;

/**
 * Se lanza cuando un value object o un atributo recibe un valor que viola su
 * formato o rango. Es compartida porque la validación de valores es transversal
 * a todos los bounded contexts.
 */
public class ValorInvalidoException extends DomainException {

    public ValorInvalidoException(String mensaje) {
        super(mensaje);
    }
}
