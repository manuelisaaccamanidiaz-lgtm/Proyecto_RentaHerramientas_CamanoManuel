package com.rentaherramientas.application.notification.usecase;

import com.rentaherramientas.domain.shared.event.DomainEvent;

/**
 * Caso de uso: convertir un evento de dominio ya confirmado en las
 * notificaciones de sus interesados.
 */
public interface NotificarEventoUseCase {

    /** Crea las notificaciones que correspondan al evento; los eventos sin aviso se ignoran. */
    void notificar(DomainEvent evento);
}
