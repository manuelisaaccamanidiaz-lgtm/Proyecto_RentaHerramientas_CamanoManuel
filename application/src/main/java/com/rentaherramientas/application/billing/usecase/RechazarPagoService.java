package com.rentaherramientas.application.billing.usecase;

import com.rentaherramientas.application.billing.dto.PagoResponse;
import com.rentaherramientas.application.billing.exception.PagoNoEncontradoException;
import com.rentaherramientas.application.billing.port.ConsultaReservas;
import com.rentaherramientas.application.billing.port.ReservaFacturable;
import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.domain.billing.model.aggregate.Pago;
import com.rentaherramientas.domain.billing.port.repository.PagoRepository;

/**
 * Rechaza un pago PENDIENTE (el agregado rechaza los ya resueltos con 409). Un
 * pago RECHAZADO deja de ser vigente, así que la reserva puede pagarse de nuevo.
 */
public class RechazarPagoService implements RechazarPagoUseCase {

    private final PagoRepository pagoRepository;
    private final ConsultaReservas consultaReservas;

    public RechazarPagoService(PagoRepository pagoRepository, ConsultaReservas consultaReservas) {
        this.pagoRepository = pagoRepository;
        this.consultaReservas = consultaReservas;
    }

    @Override
    public PagoResponse rechazar(Long pagoId, Solicitante solicitante) {
        Pago pago = pagoRepository.buscarPorId(pagoId).orElseThrow(() -> new PagoNoEncontradoException(pagoId));
        ReservaFacturable reserva = AccesoFacturacion.cargarReserva(consultaReservas, pago.getReservaId());
        AccesoFacturacion.exigirGestionar(reserva, solicitante);
        pago.rechazar();
        return PagoResponse.desde(pagoRepository.guardar(pago), null);
    }
}
