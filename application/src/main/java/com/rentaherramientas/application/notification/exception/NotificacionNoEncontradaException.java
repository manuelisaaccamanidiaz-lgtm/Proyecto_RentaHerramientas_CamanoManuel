package com.rentaherramientas.application.notification.exception;

import com.rentaherramientas.application.shared.exception.RecursoNoEncontradoException;

/**
 * Se lanza cuando la notificación no existe o no es del solicitante: para otro
 * usuario, una notificación ajena es indistinguible de una inexistente (404).
 */
public class NotificacionNoEncontradaException extends RecursoNoEncontradoException {

    /** Crea la excepción indicando el id pedido. */
    public NotificacionNoEncontradaException(Long id) {
        super("No existe la notificación " + id);
    }
}
