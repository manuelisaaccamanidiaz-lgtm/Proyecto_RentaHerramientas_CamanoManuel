package com.rentaherramientas.domain.notification.port.repository;

import com.rentaherramientas.domain.notification.model.aggregate.Notificacion;
import com.rentaherramientas.domain.shared.model.Pagina;
import com.rentaherramientas.domain.shared.model.SolicitudPagina;

import java.util.Optional;

/**
 * Puerto de persistencia del agregado Notificacion.
 */
public interface NotificacionRepository {

    /** Inserta o actualiza la notificación y la devuelve con su id. */
    Notificacion guardar(Notificacion notificacion);

    /** Busca una notificación por id; vacío si no existe. */
    Optional<Notificacion> buscarPorId(Long id);

    /** Notificaciones del usuario, de la más reciente a la más antigua; opcionalmente solo las no leídas. */
    Pagina<Notificacion> buscarDelUsuario(Long usuarioId, boolean soloNoLeidas, SolicitudPagina pagina);

    /** Cuántas notificaciones sin leer tiene el usuario. */
    long contarNoLeidas(Long usuarioId);

    /** Marca como leídas todas las notificaciones pendientes del usuario. */
    void marcarTodasLeidas(Long usuarioId);
}
