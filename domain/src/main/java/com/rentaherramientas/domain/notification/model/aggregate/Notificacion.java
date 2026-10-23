package com.rentaherramientas.domain.notification.model.aggregate;

import com.rentaherramientas.domain.shared.model.Validaciones;
import com.rentaherramientas.domain.shared.model.aggregate.AggregateRoot;

import java.time.LocalDateTime;

/**
 * Raíz del agregado Notificacion: un aviso dentro de la app para un usuario
 * (identity, referenciado solo por id). Nace sin leer y lo único que cambia
 * después es que su dueño la marque como leída.
 */
public final class Notificacion extends AggregateRoot {

    public static final int LONGITUD_MAXIMA_MENSAJE = 300;

    private final Long id;
    private final Long usuarioId;
    private final String mensaje;
    private boolean leida;
    private final LocalDateTime creadaEn;

    /** Constructor privado: valida destinatario, mensaje (no vacío, hasta 300 caracteres) y fecha. */
    private Notificacion(Long id, Long usuarioId, String mensaje, boolean leida, LocalDateTime creadaEn) {
        this.id = id;
        this.usuarioId = Validaciones.requerido(usuarioId, "usuarioId");
        this.mensaje = Validaciones.textoRequerido(mensaje, "mensaje", LONGITUD_MAXIMA_MENSAJE);
        this.leida = leida;
        this.creadaEn = Validaciones.requerido(creadaEn, "creadaEn");
    }

    /** Crea una notificación nueva, sin leer. */
    public static Notificacion nueva(Long usuarioId, String mensaje, LocalDateTime creadaEn) {
        return new Notificacion(null, usuarioId, mensaje, false, creadaEn);
    }

    /** Reconstruye una notificación leída de la base de datos. */
    public static Notificacion reconstruir(Long id, Long usuarioId, String mensaje, boolean leida,
                                           LocalDateTime creadaEn) {
        return new Notificacion(Validaciones.requerido(id, "id"), usuarioId, mensaje, leida, creadaEn);
    }

    /** Marca la notificación como leída; repetirlo no tiene efecto. */
    public void marcarLeida() {
        this.leida = true;
    }

    /** Indica si la notificación es para el usuario dado. */
    public boolean perteneceA(Long usuarioId) {
        return this.usuarioId.equals(usuarioId);
    }

    /** Identificador; null si aún no se ha guardado. */
    public Long getId() {
        return id;
    }

    /** Id del usuario destinatario. */
    public Long getUsuarioId() {
        return usuarioId;
    }

    /** Texto que ve el usuario. */
    public String getMensaje() {
        return mensaje;
    }

    /** Indica si el usuario ya la leyó. */
    public boolean isLeida() {
        return leida;
    }

    /** Momento en que se creó. */
    public LocalDateTime getCreadaEn() {
        return creadaEn;
    }
}
