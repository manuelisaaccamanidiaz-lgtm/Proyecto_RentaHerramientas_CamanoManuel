package com.rentaherramientas.application.notification.dto;

import com.rentaherramientas.domain.notification.model.aggregate.Notificacion;

import java.time.LocalDateTime;

/**
 * Notificación tal como la ve su dueño en la API.
 */
public record NotificacionResponse(Long id, String mensaje, boolean leida, LocalDateTime creadaEn) {

    /** Copia los datos del agregado. */
    public static NotificacionResponse desde(Notificacion notificacion) {
        return new NotificacionResponse(notificacion.getId(), notificacion.getMensaje(), notificacion.isLeida(),
                notificacion.getCreadaEn());
    }
}
