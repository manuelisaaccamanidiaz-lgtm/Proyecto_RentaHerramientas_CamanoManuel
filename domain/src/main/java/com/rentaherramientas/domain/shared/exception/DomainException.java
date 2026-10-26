package com.rentaherramientas.domain.shared.exception;

/**
 * Raíz de todas las excepciones de reglas de negocio. Tenerla separada de las
 * excepciones técnicas permite que los adaptadores de entrada las traduzcan a
 * respuestas de negocio (p. ej. 409/422) sin capturar errores de infraestructura.
 *
 * TÉCNICA AVANZADA (Java): excepción no comprobada (unchecked)
 *   Qué hace: al heredar de `RuntimeException` no obliga a declarar `throws` en cada firma, así las reglas de
 *     negocio atraviesan casos de uso y transacciones sin ensuciar el código. Además, Spring hace rollback
 *     automático de `@Transactional` ante cualquier `RuntimeException`.
 *   Cómo usarlo: crea una subclase por regla de negocio (`extends DomainException`) y tradúcela a HTTP en un
 *     único `@RestControllerAdvice`.
 */
public abstract class DomainException extends RuntimeException {

    /** Constructor protegido: solo las subclases concretas pueden crear la excepción. */
    protected DomainException(String mensaje) {
        super(mensaje);
    }
}
