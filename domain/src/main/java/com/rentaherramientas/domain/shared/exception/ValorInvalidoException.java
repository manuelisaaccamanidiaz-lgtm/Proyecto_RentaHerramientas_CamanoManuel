package com.rentaherramientas.domain.shared.exception;

/**
 * Se lanza cuando un value object o un atributo recibe un valor que viola su
 * formato o rango. Es compartida porque la validación de valores es transversal
 * a todos los bounded contexts.
 */
public class ValorInvalidoException extends DomainException {

    /** Crea la excepción con el mensaje que verá el cliente de la API. */
    public ValorInvalidoException(String mensaje) {
        super(mensaje);
    }
}
