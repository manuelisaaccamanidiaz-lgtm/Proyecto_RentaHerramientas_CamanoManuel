package com.rentaherramientas.application.notification.usecase;

import com.rentaherramientas.application.notification.dto.NotificacionResponse;
import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.application.shared.dto.PaginaResponse;

/**
 * Caso de uso: cada usuario consulta y marca como leídas SUS notificaciones.
 */
public interface GestionarNotificacionesUseCase {

    /** Notificaciones del solicitante, de la más reciente a la más antigua. */
    PaginaResponse<NotificacionResponse> mias(Solicitante solicitante, boolean soloNoLeidas, int pagina, int tamano);

    /** Cuántas notificaciones sin leer tiene el solicitante. */
    long contarNoLeidas(Solicitante solicitante);

    /** Marca una notificación como leída; si no existe o no es suya → 404. */
    void marcarLeida(Long notificacionId, Solicitante solicitante);

    /** Marca como leídas todas las notificaciones del solicitante. */
    void marcarTodasLeidas(Solicitante solicitante);
}
