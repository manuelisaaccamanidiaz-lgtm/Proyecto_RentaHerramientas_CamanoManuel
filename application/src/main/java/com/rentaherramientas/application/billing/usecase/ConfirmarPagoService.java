package com.rentaherramientas.application.billing.usecase;

import com.rentaherramientas.application.billing.dto.PagoResponse;
import com.rentaherramientas.application.billing.exception.PagoNoEncontradoException;
import com.rentaherramientas.application.billing.exception.ReservaNoPagableException;
import com.rentaherramientas.application.billing.port.ConsultaReservas;
import com.rentaherramientas.application.billing.port.GeneradorNumeroFactura;
import com.rentaherramientas.application.billing.port.ReservaFacturable;
import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.application.shared.port.UnidadDeTrabajo;
import com.rentaherramientas.domain.billing.model.aggregate.Factura;
import com.rentaherramientas.domain.billing.model.aggregate.Pago;
import com.rentaherramientas.domain.billing.port.repository.FacturaRepository;
import com.rentaherramientas.domain.billing.port.repository.PagoRepository;

import java.time.Clock;

/**
 * Aprueba un pago PENDIENTE (el agregado rechaza los ya resueltos con 409) y
 * emite su factura en la misma unidad de trabajo. La reserva debe seguir
 * ACEPTADA: no se cobra una reserva que venció mientras el efectivo estaba pendiente.
 */
public class ConfirmarPagoService implements ConfirmarPagoUseCase {

    private final PagoRepository pagoRepository;
    private final ConsultaReservas consultaReservas;
    private final UnidadDeTrabajo unidadDeTrabajo;
    private final EmisorFactura emisorFactura;

    public ConfirmarPagoService(PagoRepository pagoRepository, FacturaRepository facturaRepository,
                                ConsultaReservas consultaReservas, GeneradorNumeroFactura generadorNumeroFactura,
                                UnidadDeTrabajo unidadDeTrabajo, Clock clock) {
        this.pagoRepository = pagoRepository;
        this.consultaReservas = consultaReservas;
        this.unidadDeTrabajo = unidadDeTrabajo;
        this.emisorFactura = new EmisorFactura(facturaRepository, generadorNumeroFactura, clock);
    }

    @Override
    public PagoResponse confirmar(Long pagoId, Solicitante solicitante) {
        Pago pago = pagoRepository.buscarPorId(pagoId).orElseThrow(() -> new PagoNoEncontradoException(pagoId));
        ReservaFacturable reserva = AccesoFacturacion.cargarReserva(consultaReservas, pago.getReservaId());
        AccesoFacturacion.exigirGestionar(reserva, solicitante);
        if (!reserva.estaAceptada()) {
            throw new ReservaNoPagableException(reserva.reservaId(), reserva.estado());
        }
        return unidadDeTrabajo.ejecutar(() -> {
            pago.aprobar();
            Pago guardado = pagoRepository.guardar(pago);
            Factura factura = emisorFactura.emitir(guardado, reserva);
            return PagoResponse.desde(guardado, factura);
        });
    }
}
