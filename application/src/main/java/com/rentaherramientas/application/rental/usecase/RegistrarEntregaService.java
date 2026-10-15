package com.rentaherramientas.application.rental.usecase;

import com.rentaherramientas.application.rental.dto.ReservaResponse;
import com.rentaherramientas.application.rental.port.CatalogoHerramientas;
import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.domain.rental.model.aggregate.Reserva;
import com.rentaherramientas.domain.rental.port.repository.ReservaRepository;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Registra la entrega con la hora actual del reloj inyectado (zona de Bogotá).
 */
public class RegistrarEntregaService implements RegistrarEntregaUseCase {

    private final ReservaRepository reservaRepository;
    private final CatalogoHerramientas catalogoHerramientas;
    private final Clock clock;

    public RegistrarEntregaService(ReservaRepository reservaRepository, CatalogoHerramientas catalogoHerramientas,
                                   Clock clock) {
        this.reservaRepository = reservaRepository;
        this.catalogoHerramientas = catalogoHerramientas;
        this.clock = clock;
    }

    @Override
    public ReservaResponse entregar(Long reservaId, Solicitante solicitante) {
        Reserva reserva = PropiedadReserva.cargarParaGestionar(reservaRepository, catalogoHerramientas,
                reservaId, solicitante);
        reserva.registrarEntrega(LocalDateTime.now(clock));
        return ReservaResponse.desde(reservaRepository.guardar(reserva));
    }
}
