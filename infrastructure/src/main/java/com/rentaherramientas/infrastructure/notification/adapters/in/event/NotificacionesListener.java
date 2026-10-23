package com.rentaherramientas.infrastructure.notification.adapters.in.event;

import com.rentaherramientas.application.notification.usecase.NotificarEventoUseCase;
import com.rentaherramientas.domain.shared.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Adaptador de entrada: escucha los eventos de dominio y crea las notificaciones
 * SOLO cuando la transacción que los produjo se confirmó.
 *
 * Por qué AFTER_COMMIT y no `@EventListener`: un `@EventListener` se ejecuta en el mismo instante de
 *   `publishEvent`, todavía dentro de la transacción del caso de uso. Si después algo falla (p. ej. la
 *   emisión de la factura) y se hace rollback, la notificación ya existiría avisando de algo que no
 *   pasó; y si el que falla es el listener, su excepción tumbaría la operación del usuario. Con
 *   `@TransactionalEventListener(phase = AFTER_COMMIT)` Spring guarda el evento y lo entrega cuando el
 *   commit terminó bien; si hubo rollback, el evento se descarta.
 *
 * Dos detalles de esta fase:
 *   - La transacción original ya está confirmada pero sus recursos siguen enlazados al hilo: lo que se
 *     escriba "uniéndose" a ella (REQUIRED) nunca se confirmaría. Por eso se abre una transacción NUEVA
 *     (REQUIRES_NEW) para guardar las notificaciones.
 *   - Una excepción lanzada aquí llegaría al llamador aunque los datos ya estén guardados (el usuario
 *     vería un 500 de una operación que sí ocurrió). Por eso se captura todo, se registra con log.error y
 *     se sigue: perder un aviso es preferible a fallar una reserva o un pago ya confirmados.
 */
@Component
public class NotificacionesListener {

    private static final Logger log = LoggerFactory.getLogger(NotificacionesListener.class);

    private final NotificarEventoUseCase notificarEvento;
    private final TransactionTemplate transaccionNueva;

    /** Recibe el caso de uso y arma una plantilla de transacción con propagación REQUIRES_NEW. */
    public NotificacionesListener(NotificarEventoUseCase notificarEvento,
                                  PlatformTransactionManager transactionManager) {
        this.notificarEvento = notificarEvento;
        this.transaccionNueva = new TransactionTemplate(transactionManager);
        this.transaccionNueva.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /**
     * Recibe cualquier DomainEvent (el caso de uso ignora los que no avisan a nadie). El try/catch envuelve
     * también el commit de la transacción nueva, para que ni un fallo al confirmar escape al usuario.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alConfirmarse(DomainEvent evento) {
        try {
            transaccionNueva.executeWithoutResult(estado -> notificarEvento.notificar(evento));
        } catch (RuntimeException e) {
            log.error("No se pudieron crear las notificaciones del evento {}", evento, e);
        }
    }
}
