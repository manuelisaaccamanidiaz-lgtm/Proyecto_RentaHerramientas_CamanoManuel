package com.rentaherramientas.domain.rental.port.repository;

import java.util.List;
import java.util.Optional;

import com.rentaherramientas.domain.rental.model.aggregate.Reserva;
import com.rentaherramientas.domain.rental.model.valueobject.RangoFechas;

/**
 * Puerto de salida del agregado Reserva.
 */
public interface ReservaRepository {

    Reserva guardar(Reserva reserva);

    Optional<Reserva> buscarPorId(Long id);

    List<Reserva> buscarPorClienteId(Long clienteId);

    List<Reserva> buscarPorHerramientaId(Long herramientaId);

    /**
     * Indica si la herramienta ya tiene una reserva que bloquea disponibilidad
     * ({@link com.rentaherramientas.domain.rental.model.valueobject.EstadoReserva#bloqueaDisponibilidad})
     * y cuyas fechas se cruzan con el rango dado.
     */
    boolean existeSolapamiento(Long herramientaId, RangoFechas rango);
}
