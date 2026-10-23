package com.rentaherramientas.application.rental.usecase;

import com.rentaherramientas.application.rental.dto.ReservaResponse;
import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.application.shared.port.PublicadorEventos;
import com.rentaherramientas.application.shared.port.UnidadDeTrabajo;
import com.rentaherramientas.domain.rental.model.aggregate.Reserva;
import com.rentaherramientas.domain.rental.port.repository.ReservaRepository;

/**
 * El cliente cancela su propia reserva; la transición la valida el agregado y
 * ReservaCancelada se publica en la misma unidad de trabajo que el guardado.
 */
public class CancelarReservaService implements CancelarReservaUseCase{
    private final ReservaRepository reservaRepository;
    private final UnidadDeTrabajo unidadDeTrabajo;
    private final PublicadorEventos publicadorEventos;

    /** Recibe sus dependencias por constructor; el bean lo crea RentalBeansConfig. */
    public CancelarReservaService(ReservaRepository reservaRepository, UnidadDeTrabajo unidadDeTrabajo,
                                  PublicadorEventos publicadorEventos) {
        this.reservaRepository = reservaRepository;
        this.unidadDeTrabajo = unidadDeTrabajo;
        this.publicadorEventos = publicadorEventos;
    }

    @Override
    public ReservaResponse cancelar(Long reservaId, Solicitante solicitante){
        Reserva reserva = PropiedadReserva.cargarDelCliente(reservaRepository, reservaId, solicitante);
        reserva.cancelar();
        return unidadDeTrabajo.ejecutar(() -> {
            Reserva guardada = reservaRepository.guardar(reserva);
            publicadorEventos.publicar(reserva.extraerEventos());
            return ReservaResponse.desde(guardada, false);
        });
    }
}
