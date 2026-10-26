package com.rentaherramientas.application.rental.usecase;

import com.rentaherramientas.application.rental.dto.ReservaResponse;
import com.rentaherramientas.application.rental.port.CatalogoHerramientas;
import com.rentaherramientas.application.rental.port.ConsultaPagos;
import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.application.shared.port.PublicadorEventos;
import com.rentaherramientas.application.shared.port.UnidadDeTrabajo;
import com.rentaherramientas.domain.rental.model.aggregate.Reserva;
import com.rentaherramientas.domain.rental.port.repository.ReservaRepository;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Registra la devolución con la hora actual del reloj inyectado. Devolver una
 * reserva que no fue entregada lo rechaza la máquina de estados del agregado (409).
 */
public class RegistrarDevolucionService implements RegistrarDevolucionUseCase {

    private final ReservaRepository reservaRepository;
    private final CatalogoHerramientas catalogoHerramientas;
    private final ConsultaPagos consultaPagos;
    private final UnidadDeTrabajo unidadDeTrabajo;
    private final PublicadorEventos publicadorEventos;
    private final Clock clock;

    /** Recibe sus dependencias por constructor; el bean lo crea RentalBeansConfig. */
    public RegistrarDevolucionService(ReservaRepository reservaRepository,
                                      CatalogoHerramientas catalogoHerramientas, ConsultaPagos consultaPagos,
                                      UnidadDeTrabajo unidadDeTrabajo,
                                      PublicadorEventos publicadorEventos, Clock clock) {
        this.reservaRepository = reservaRepository;
        this.catalogoHerramientas = catalogoHerramientas;
        this.consultaPagos = consultaPagos;
        this.unidadDeTrabajo = unidadDeTrabajo;
        this.publicadorEventos = publicadorEventos;
        this.clock = clock;
    }

    /** Registra que el cliente devolvió la herramienta, con la hora actual del reloj inyectado. */
    @Override
    public ReservaResponse devolver(Long reservaId, Solicitante solicitante) {
        Reserva reserva = PropiedadReserva.cargarParaGestionar(reservaRepository, catalogoHerramientas,
                reservaId, solicitante);
        reserva.registrarDevolucion(LocalDateTime.now(clock));
        return unidadDeTrabajo.ejecutar(() -> {
            Reserva guardada = reservaRepository.guardar(reserva);
            publicadorEventos.publicar(reserva.extraerEventos());
            return ReservaResponse.desde(guardada, consultaPagos.tienePagoAprobado(reservaId));
        });
    }
}
