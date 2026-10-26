package com.rentaherramientas.infrastructure.shared.adapters.out.event;

import com.rentaherramientas.application.shared.port.PublicadorEventos;
import com.rentaherramientas.domain.shared.event.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Implementa el puerto con el bus de eventos en memoria de Spring. Los records
 * de dominio no heredan de ApplicationEvent: Spring los envuelve y los entrega a
 * los métodos anotados que reciben ese tipo (o una interfaz que implementen,
 * como DomainEvent).
 *
 * TÉCNICA AVANZADA (Spring): `ApplicationEventPublisher` + `@TransactionalEventListener`
 *   Qué hace: `publishEvent` es síncrono. Si hay una transacción activa, los listeners transaccionales no
 *     se ejecutan aquí: Spring los apunta en la transacción y los llama en la fase indicada (p. ej. tras
 *     el commit). Si NO hay transacción, esos listeners descartan el evento; por eso los casos de uso
 *     publican dentro de su UnidadDeTrabajo.
 */
@Component
public class PublicadorEventosSpring implements PublicadorEventos {

    private final ApplicationEventPublisher publisher;

    /** Recibe el publicador de eventos del contexto de Spring. */
    public PublicadorEventosSpring(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publicar(List<DomainEvent> eventos) {
        eventos.forEach(publisher::publishEvent);
    }
}
