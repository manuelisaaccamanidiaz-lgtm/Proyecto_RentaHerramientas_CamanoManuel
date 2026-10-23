package com.rentaherramientas.application.notification.port;

/**
 * Vista de una reserva desde notification: sus dos partes y la herramienta
 * reservada. La usan los eventos que solo traen el id de la reserva (pagos).
 */
public record ReservaNotificable(Long reservaId, Long clienteId, Long proveedorId, String nombreHerramienta) {
}
