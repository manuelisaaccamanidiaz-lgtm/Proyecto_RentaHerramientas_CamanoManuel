package com.rentaherramientas.infrastructure.notification.adapters.out.destinatarios;

import com.rentaherramientas.application.notification.port.ConsultaDestinatarios;
import com.rentaherramientas.application.notification.port.HerramientaNotificable;
import com.rentaherramientas.application.notification.port.ReservaNotificable;
import com.rentaherramientas.domain.billing.model.aggregate.Factura;
import com.rentaherramientas.domain.billing.model.aggregate.Pago;
import com.rentaherramientas.domain.billing.model.valueobject.NumeroFactura;
import com.rentaherramientas.domain.billing.port.repository.FacturaRepository;
import com.rentaherramientas.domain.billing.port.repository.PagoRepository;
import com.rentaherramientas.domain.catalog.port.repository.HerramientaRepository;
import com.rentaherramientas.domain.rental.port.repository.ReservaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador anticorrupción de notification: traduce herramientas (catalog),
 * reservas (rental) y facturas (billing) a los datos mínimos para redactar un
 * aviso. Solo aquí, en infraestructura, notification "toca" esos BC.
 */
@Component
public class ConsultaDestinatariosAdapter implements ConsultaDestinatarios {

    private final HerramientaRepository herramientaRepository;
    private final ReservaRepository reservaRepository;
    private final PagoRepository pagoRepository;
    private final FacturaRepository facturaRepository;

    /** Recibe los repositorios de los otros BC por constructor. */
    public ConsultaDestinatariosAdapter(HerramientaRepository herramientaRepository,
                                        ReservaRepository reservaRepository, PagoRepository pagoRepository,
                                        FacturaRepository facturaRepository) {
        this.herramientaRepository = herramientaRepository;
        this.reservaRepository = reservaRepository;
        this.pagoRepository = pagoRepository;
        this.facturaRepository = facturaRepository;
    }

    @Override
    public Optional<HerramientaNotificable> herramienta(Long herramientaId) {
        return herramientaRepository.buscarPorId(herramientaId)
                .map(h -> new HerramientaNotificable(h.getNombre(), h.getProveedorId()));
    }

    @Override
    public Optional<ReservaNotificable> reserva(Long reservaId) {
        return reservaRepository.buscarPorId(reservaId).flatMap(reserva ->
                herramientaRepository.buscarPorId(reserva.getHerramientaId())
                        .map(h -> new ReservaNotificable(reserva.getId(), reserva.getClienteId(),
                                h.getProveedorId(), h.getNombre())));
    }

    /** Una reserva tiene como máximo un pago APROBADO (índice uq_pagos_reserva_vigente). */
    @Override
    public Optional<String> numeroFacturaDeReserva(Long reservaId) {
        return pagoRepository.buscarPorReservaId(reservaId).stream()
                .filter(Pago::estaAprobado)
                .findFirst()
                .flatMap(pago -> facturaRepository.buscarPorPagoId(pago.getId()))
                .map(Factura::getNumero)
                .map(NumeroFactura::valor);
    }
}
