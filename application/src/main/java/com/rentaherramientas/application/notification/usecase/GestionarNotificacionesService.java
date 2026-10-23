package com.rentaherramientas.application.notification.usecase;

import com.rentaherramientas.application.notification.dto.NotificacionResponse;
import com.rentaherramientas.application.notification.exception.NotificacionNoEncontradaException;
import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.application.shared.dto.PaginaResponse;
import com.rentaherramientas.domain.notification.model.aggregate.Notificacion;
import com.rentaherramientas.domain.notification.port.repository.NotificacionRepository;
import com.rentaherramientas.domain.shared.model.SolicitudPagina;

/**
 * Consulta y lectura de notificaciones. Todo se filtra por el id del
 * solicitante (del JWT), así que nadie ve ni marca avisos ajenos; ni siquiera un
 * ADMIN, porque las notificaciones son personales.
 */
public class GestionarNotificacionesService implements GestionarNotificacionesUseCase {

    private final NotificacionRepository notificacionRepository;

    /** Recibe el repositorio por constructor; el bean lo crea NotificationBeansConfig. */
    public GestionarNotificacionesService(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    @Override
    public PaginaResponse<NotificacionResponse> mias(Solicitante solicitante, boolean soloNoLeidas, int pagina,
                                                     int tamano) {
        return PaginaResponse.desde(notificacionRepository.buscarDelUsuario(solicitante.id(), soloNoLeidas,
                new SolicitudPagina(pagina, tamano)), NotificacionResponse::desde);
    }

    @Override
    public long contarNoLeidas(Solicitante solicitante) {
        return notificacionRepository.contarNoLeidas(solicitante.id());
    }

    /** Una notificación ajena responde igual que una inexistente: no se revela que exista. */
    @Override
    public void marcarLeida(Long notificacionId, Solicitante solicitante) {
        Notificacion notificacion = notificacionRepository.buscarPorId(notificacionId)
                .filter(n -> n.perteneceA(solicitante.id()))
                .orElseThrow(() -> new NotificacionNoEncontradaException(notificacionId));
        if (!notificacion.isLeida()) {
            notificacion.marcarLeida();
            notificacionRepository.guardar(notificacion);
        }
    }

    @Override
    public void marcarTodasLeidas(Solicitante solicitante) {
        notificacionRepository.marcarTodasLeidas(solicitante.id());
    }
}
