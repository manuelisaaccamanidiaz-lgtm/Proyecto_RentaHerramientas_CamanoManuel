package com.rentaherramientas.domain.rental.port.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.rentaherramientas.domain.rental.model.aggregate.Reserva;
import com.rentaherramientas.domain.rental.model.valueobject.RangoFechas;
import com.rentaherramientas.domain.shared.model.Pagina;

/**
 * Puerto de salida del agregado Reserva.
 */
public interface ReservaRepository {

    /**
     * Inserta o actualiza. Si la BD detecta que la reserva se cruza con otra
     * activa de la misma herramienta (carrera entre dos altas simultáneas),
     * lanza ReservaSolapadaException.
     */
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

    /** Búsqueda paginada con filtros opcionales, de la más reciente a la más antigua. */
    Pagina<Reserva> buscar(CriteriosBusquedaReserva criterios);

    /** Periodos de las reservas que bloquean la herramienta y se cruzan con el periodo dado, por fecha. */
    List<RangoFechas> rangosOcupados(Long herramientaId, RangoFechas periodo);

    /**
     * Reservas que ya no pueden cumplirse a fecha de hoy: PENDIENTE que debían
     * empezar antes de hoy y ACEPTADA (aún sin entregar) que debían terminar antes de hoy.
     */
    List<Reserva> buscarVencibles(LocalDate hoy);
}
