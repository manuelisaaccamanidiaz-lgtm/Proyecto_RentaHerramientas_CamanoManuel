package com.rentaherramientas.application.rental.usecase;

import com.rentaherramientas.domain.rental.model.aggregate.Reserva;
import com.rentaherramientas.domain.rental.port.repository.ReservaRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Vence, según el "hoy" del reloj inyectado: las PENDIENTE cuya fecha de inicio
 * ya pasó sin respuesta del proveedor, y las ACEPTADA que terminaron sin
 * entregarse. Al vencer dejan de bloquear las fechas de la herramienta. Cada
 * reserva pasa por el agregado, que valida la transición.
 */
public class VencerReservasService implements VencerReservasUseCase {

    private final ReservaRepository reservaRepository;
    private final Clock clock;

    /** Recibe sus dependencias por constructor; el bean lo crea RentalBeansConfig. */
    public VencerReservasService(ReservaRepository reservaRepository, Clock clock) {
        this.reservaRepository = reservaRepository;
        this.clock = clock;
    }

    /** Vence todas las reservas cuya fecha ya pasó sin entregarse y devuelve cuántas venció. */
    @Override
    public int vencer() {
        List<Reserva> vencibles = reservaRepository.buscarVencibles(LocalDate.now(clock));
        for (Reserva reserva : vencibles) {
            reserva.vencer();
            reservaRepository.guardar(reserva);
        }
        return vencibles.size();
    }
}
