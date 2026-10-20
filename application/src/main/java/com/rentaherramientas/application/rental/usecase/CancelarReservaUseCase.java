package com.rentaherramientas.application.rental.usecase;

import com.rentaherramientas.application.rental.dto.ReservaResponse;
import com.rentaherramientas.application.shared.command.Solicitante;

public interface CancelarReservaUseCase {
        ReservaResponse cancelar(Long reservaId, Solicitante solicitante);

}
