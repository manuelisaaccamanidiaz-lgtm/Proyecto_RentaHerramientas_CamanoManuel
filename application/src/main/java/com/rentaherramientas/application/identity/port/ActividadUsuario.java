package com.rentaherramientas.application.identity.port;

/**
 * Puerto anticorrupción: identity pregunta si un usuario tiene historial en
 * otros BC (rental, catalog) sin depender de sus clases. Solo devuelve sí o no;
 * la implementación decide cómo averiguarlo (consultas a la BD).
 */
public interface ActividadUsuario {

    /** Indica si el usuario tiene al menos una reserva registrada como cliente (en cualquier estado). */
    boolean tieneReservasComoCliente(Long usuarioId);

    /** Indica si el usuario tiene al menos una herramienta registrada como proveedor (en cualquier estado). */
    boolean tieneHerramientasComoProveedor(Long usuarioId);
}
