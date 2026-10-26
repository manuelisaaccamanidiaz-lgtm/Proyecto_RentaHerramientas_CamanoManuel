package com.rentaherramientas.application.billing.usecase;

import com.rentaherramientas.application.billing.dto.PagoResponse;
import com.rentaherramientas.application.billing.exception.PagoNoEncontradoException;
import com.rentaherramientas.application.billing.exception.ReservaNoPagableException;
import com.rentaherramientas.application.billing.port.ConsultaReservas;
import com.rentaherramientas.application.billing.port.GeneradorNumeroFactura;
import com.rentaherramientas.application.billing.port.ReservaFacturable;
import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.application.shared.port.PublicadorEventos;
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
    private final PublicadorEventos publicadorEventos;
    private final EmisorFactura emisorFactura;

    /** Recibe sus dependencias por constructor (el bean lo crea BillingBeansConfig) y arma su EmisorFactura. */
    public ConfirmarPagoService(PagoRepository pagoRepository, FacturaRepository facturaRepository,
                                ConsultaReservas consultaReservas, GeneradorNumeroFactura generadorNumeroFactura,
                                UnidadDeTrabajo unidadDeTrabajo,
                                PublicadorEventos publicadorEventos, Clock clock) {
        this.pagoRepository = pagoRepository;
        this.consultaReservas = consultaReservas;
        this.unidadDeTrabajo = unidadDeTrabajo;
        this.publicadorEventos = publicadorEventos;
        this.emisorFactura = new EmisorFactura(facturaRepository, generadorNumeroFactura, clock);
    }

    /**
     * Aprueba un pago pendiente y emite su factura en la misma transacción.
     *
     * Técnica avanzada — transacción programática con lambda: todo lo que va dentro de
     *   `unidadDeTrabajo.ejecutar(() -> { ... })` se confirma junto o se deshace junto: no puede quedar un
     *   pago aprobado sin factura. Las validaciones de lectura se hacen antes, fuera de la transacción, para
     *   mantenerla corta.
     */
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
            publicadorEventos.publicar(pago.extraerEventos());
            return PagoResponse.desde(guardado, factura);
        });
    }
}
