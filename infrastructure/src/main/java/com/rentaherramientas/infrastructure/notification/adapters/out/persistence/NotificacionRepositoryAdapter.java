package com.rentaherramientas.infrastructure.notification.adapters.out.persistence;

import com.rentaherramientas.domain.notification.model.aggregate.Notificacion;
import com.rentaherramientas.domain.notification.port.repository.NotificacionRepository;
import com.rentaherramientas.domain.shared.model.Pagina;
import com.rentaherramientas.domain.shared.model.SolicitudPagina;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Adaptador de salida que implementa el puerto NotificacionRepository con JPA.
 */
@Repository
public class NotificacionRepositoryAdapter implements NotificacionRepository {

    private static final Sort MAS_RECIENTES = Sort.by(Sort.Order.desc("creadaEn"), Sort.Order.desc("id"));

    private final NotificacionJpaRepository jpaRepository;

    /** Recibe el repositorio de Spring Data por constructor. */
    public NotificacionRepositoryAdapter(NotificacionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public Notificacion guardar(Notificacion notificacion) {
        return toDomain(jpaRepository.save(new NotificacionEntity(notificacion.getId(), notificacion.getUsuarioId(),
                notificacion.getMensaje(), notificacion.isLeida(), notificacion.getCreadaEn())));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Notificacion> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(NotificacionRepositoryAdapter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Pagina<Notificacion> buscarDelUsuario(Long usuarioId, boolean soloNoLeidas, SolicitudPagina solicitud) {
        PageRequest pageable = PageRequest.of(solicitud.numero(), solicitud.tamano(), MAS_RECIENTES);
        Page<NotificacionEntity> pagina = soloNoLeidas
                ? jpaRepository.findByUsuarioIdAndLeida(usuarioId, false, pageable)
                : jpaRepository.findByUsuarioId(usuarioId, pageable);
        return new Pagina<>(pagina.getContent().stream().map(NotificacionRepositoryAdapter::toDomain).toList(),
                solicitud.numero(), solicitud.tamano(), pagina.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public long contarNoLeidas(Long usuarioId) {
        return jpaRepository.countByUsuarioIdAndLeida(usuarioId, false);
    }

    @Override
    @Transactional
    public void marcarTodasLeidas(Long usuarioId) {
        jpaRepository.marcarTodasLeidas(usuarioId);
    }

    /** El agregado es plano (sin value objects), así que no hace falta un mapper aparte. */
    private static Notificacion toDomain(NotificacionEntity entity) {
        return Notificacion.reconstruir(entity.getId(), entity.getUsuarioId(), entity.getMensaje(), entity.isLeida(),
                entity.getCreadaEn());
    }
}
