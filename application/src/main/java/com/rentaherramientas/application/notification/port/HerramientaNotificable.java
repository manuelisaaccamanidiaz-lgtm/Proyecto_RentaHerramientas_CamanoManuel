package com.rentaherramientas.application.notification.port;

/**
 * Vista de una herramienta desde notification: su nombre (para el texto) y su
 * proveedor (destinatario de los avisos sobre sus reservas).
 */
public record HerramientaNotificable(String nombre, Long proveedorId) {
}
