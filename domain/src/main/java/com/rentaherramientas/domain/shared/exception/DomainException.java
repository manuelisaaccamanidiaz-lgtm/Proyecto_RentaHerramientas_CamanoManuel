package com.rentaherramientas.domain.shared.exception;

/**
 * Raíz de todas las excepciones de reglas de negocio. Tenerla separada de las
 * excepciones técnicas permite que los adaptadores de entrada las traduzcan a
 * respuestas de negocio (p. ej. 409/422) sin capturar errores de infraestructura.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String mensaje) {
        super(mensaje);
    }
}
