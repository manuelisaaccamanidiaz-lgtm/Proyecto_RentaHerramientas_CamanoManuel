package com.rentaherramientas.infrastructure.notification.adapters.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio Spring Data de notificaciones, uso interno del adaptador. Las
 * consultas por (usuario_id, leida) las resuelve el índice ix_notificaciones_usuario_leida.
 */
public interface NotificacionJpaRepository extends JpaRepository<NotificacionEntity, Long> {

    /** Todas las notificaciones del usuario (el orden lo pone el Pageable). */
    Page<NotificacionEntity> findByUsuarioId(Long usuarioId, Pageable pageable);

    /** Notificaciones del usuario con ese estado de lectura. */
    Page<NotificacionEntity> findByUsuarioIdAndLeida(Long usuarioId, boolean leida, Pageable pageable);

    /** Cuántas notificaciones del usuario tienen ese estado de lectura. */
    long countByUsuarioIdAndLeida(Long usuarioId, boolean leida);

    /**
     * Marca todas las pendientes del usuario en un único UPDATE.
     *
     * TÉCNICA AVANZADA (Spring Data): `@Modifying` + `@Query` de actualización
     *   Qué hace: ejecuta un UPDATE masivo sin cargar las entidades una por una. Devuelve cuántas filas
     *     cambiaron.
     */
    @Modifying
    @Query("update NotificacionEntity n set n.leida = true where n.usuarioId = :usuarioId and n.leida = false")
    int marcarTodasLeidas(@Param("usuarioId") Long usuarioId);
}
