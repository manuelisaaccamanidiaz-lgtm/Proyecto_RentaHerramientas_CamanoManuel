package com.rentaherramientas.domain.shared.model.aggregate;

import com.rentaherramientas.domain.shared.event.DomainEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Base de las raíces de agregado: acumula los eventos de dominio que producen
 * sus métodos de negocio. Los eventos no se persisten; la capa de aplicación
 * los extrae tras guardar el agregado y decide cómo publicarlos.
 */
public abstract class AggregateRoot {

    private final List<DomainEvent> eventos = new ArrayList<>();

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
