package com.rentaherramientas.application.shared.port;

import java.util.function.Supplier;

/**
 * Puerto de salida para ejecutar varias escrituras como una sola operación
 * atómica (todo o nada). La aplicación decide QUÉ va junto; cómo se garantiza
 * (una transacción de base de datos) lo decide infraestructura. Así los casos de
 * uso siguen siendo Java puro.
 */
public interface UnidadDeTrabajo {

    /**
     * Ejecuta la operación dentro de una transacción y devuelve su resultado; si lanza una
     * excepción, se deshacen todas sus escrituras.
     *
     * TÉCNICA AVANZADA (Java): `Supplier<T>` como bloque de código
     *   Qué hace: el llamador pasa una lambda `() -> { ...; return x; }` y la implementación decide cuándo y
     *     cómo ejecutarla (aquí, envuelta en una transacción de Spring).
     *   Cómo usarlo: `unidadDeTrabajo.ejecutar(() -> repo.guardar(entidad));`
     */
    <T> T ejecutar(Supplier<T> operacion);
}
