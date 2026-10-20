package com.rentaherramientas.application.rental.usecase;

import com.rentaherramientas.application.rental.dto.ReservaResponse;
import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.domain.rental.model.aggregate.Reserva;
import com.rentaherramientas.domain.rental.port.repository.ReservaRepository;

public class CancelarReservaService implements CancelarReservaUseCase{
    private final ReservaRepository reservaRepository;

    

    public CancelarReservaService(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    @Override
    public ReservaResponse cancelar(Long reservaId, Solicitante solicitante){
        Reserva reserva = PropiedadReserva.cargarDelCliente(reservaRepository, reservaId, solicitante);
        reserva.cancelar();
        return ReservaResponse.desde(reservaRepository.guardar(reserva), false);
    }
}
