package com.rentaherramientas.application.shared.port;

import java.util.function.Supplier;

/**
 * Puerto de salida para ejecutar varias escrituras como una sola operación
 * atómica (todo o nada). La aplicación decide QUÉ va junto; cómo se garantiza
 * (una transacción de base de datos) lo decide infraestructura. Así los casos de
 * uso siguen siendo Java puro.
 */
public interface UnidadDeTrabajo {

    <T> T ejecutar(Supplier<T> operacion);
}
