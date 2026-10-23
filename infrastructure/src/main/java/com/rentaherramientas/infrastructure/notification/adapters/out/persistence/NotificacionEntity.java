package com.rentaherramientas.infrastructure.notification.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Modelo de persistencia de "notificaciones". usuario_id es un Long simple: el
 * usuario pertenece al BC identity y entre BC solo se referencia por id.
 */
@Entity
@Table(name = "notificaciones")
public class NotificacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "mensaje", nullable = false, length = 300)
    private String mensaje;

    @Column(name = "leida", nullable = false)
    private boolean leida;

    @Column(name = "creada_en", nullable = false)
    private LocalDateTime creadaEn;

    /** Constructor sin argumentos exigido por JPA. */
    protected NotificacionEntity() {
        // requerido por JPA
    }

    /** Crea la entidad a partir del agregado; un id null indica INSERT. */
    public NotificacionEntity(Long id, Long usuarioId, String mensaje, boolean leida, LocalDateTime creadaEn) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.mensaje = mensaje;
        this.leida = leida;
        this.creadaEn = creadaEn;
    }

    /** Id de la notificación. */
    public Long getId() {
        return id;
    }

    /** Id del usuario destinatario. */
    public Long getUsuarioId() {
        return usuarioId;
    }

    /** Texto de la notificación. */
    public String getMensaje() {
        return mensaje;
    }

    /** Indica si ya se leyó. */
    public boolean isLeida() {
        return leida;
    }

    /** Momento de creación. */
    public LocalDateTime getCreadaEn() {
        return creadaEn;
    }
}
