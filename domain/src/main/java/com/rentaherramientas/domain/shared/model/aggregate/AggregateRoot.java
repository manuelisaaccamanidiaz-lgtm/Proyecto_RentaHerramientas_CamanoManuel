package com.rentaherramientas.domain.shared.model.aggregate;

import com.rentaherramientas.domain.shared.event.DomainEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Base de las raíces de agregado: acumula los eventos de dominio que producen
 * sus métodos de negocio. Los eventos no se persisten; la capa de aplicación
 * los extrae tras guardar el agregado y decide cómo publicarlos.
 *
 * TÉCNICA AVANZADA (DDD): eventos de dominio acumulados en el agregado
 *   Qué hace: el agregado no publica nada por sí mismo (no conoce Spring); solo anota lo ocurrido. El
 *     servicio de aplicación, después de guardar, llama a `extraerEventos()` y los publica.
 *   Cómo usarlo: en un método de negocio llama a `registrarEvento(new MiEvento(...))`.
 */
public abstract class AggregateRoot {

    private final List<DomainEvent> eventos = new ArrayList<>();

    /** Anota un evento ocurrido en el agregado; solo las subclases pueden registrarlos. */
    protected void registrarEvento(DomainEvent evento) {
        eventos.add(Objects.requireNonNull(evento));
    }

    /** Devuelve los eventos pendientes y los limpia para no publicarlos dos veces. */
    public List<DomainEvent> extraerEventos() {
        List<DomainEvent> pendientes = List.copyOf(eventos);
        eventos.clear();
        return pendientes;
    }
}
