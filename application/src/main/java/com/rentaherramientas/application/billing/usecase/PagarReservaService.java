package com.rentaherramientas.application.billing.usecase;

import com.rentaherramientas.application.billing.command.PagarReservaCommand;
import com.rentaherramientas.application.billing.dto.PagoResponse;
import com.rentaherramientas.application.billing.exception.ReservaNoPagableException;
import com.rentaherramientas.application.billing.port.ConsultaReservas;
import com.rentaherramientas.application.billing.port.GeneradorNumeroFactura;
import com.rentaherramientas.application.billing.port.ReservaFacturable;
import com.rentaherramientas.application.shared.exception.AccesoDenegadoException;
import com.rentaherramientas.application.shared.port.UnidadDeTrabajo;
import com.rentaherramientas.domain.billing.exception.PagoDuplicadoException;
import com.rentaherramientas.domain.billing.model.aggregate.Factura;
import com.rentaherramientas.domain.billing.model.aggregate.Pago;
import com.rentaherramientas.domain.billing.model.valueobject.MetodoPago;
import com.rentaherramientas.domain.billing.model.valueobject.ReferenciaPago;
import com.rentaherramientas.domain.billing.port.repository.FacturaRepository;
import com.rentaherramientas.domain.billing.port.repository.PagoRepository;
import com.rentaherramientas.domain.identity.model.valueobject.Rol;
import com.rentaherramientas.domain.shared.model.Validaciones;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

/**
 * Registra el pago simulado de una reserva. Reglas, en orden: el solicitante es
 * CLIENTE y dueño de la reserva (403), la reserva está ACEPTADA (409) y no tiene
 * ya un pago PENDIENTE o APROBADO (409). El monto SIEMPRE es el precio total de
 * la reserva. TARJETA y PSE se aprueban al instante y, en la MISMA unidad de
 * trabajo, se emite la factura; EFECTIVO queda PENDIENTE hasta que el proveedor
 * lo confirme.
 */
public class PagarReservaService implements PagarReservaUseCase {

    private final PagoRepository pagoRepository;
    private final ConsultaReservas consultaReservas;
    private final UnidadDeTrabajo unidadDeTrabajo;
    private final EmisorFactura emisorFactura;
    private final Clock clock;

    public PagarReservaService(PagoRepository pagoRepository, FacturaRepository facturaRepository,
                               ConsultaReservas consultaReservas, GeneradorNumeroFactura generadorNumeroFactura,
                               UnidadDeTrabajo unidadDeTrabajo, Clock clock) {
        this.pagoRepository = pagoRepository;
        this.consultaReservas = consultaReservas;
        this.unidadDeTrabajo = unidadDeTrabajo;
        this.emisorFactura = new EmisorFactura(facturaRepository, generadorNumeroFactura, clock);
        this.clock = clock;
    }

    @Override
    public PagoResponse pagar(PagarReservaCommand command) {
        if (command.solicitante().rol() != Rol.CLIENTE) {
            throw new AccesoDenegadoException("Solo un CLIENTE puede pagar reservas");
        }
        MetodoPago metodo = Validaciones.requerido(command.metodo(), "metodo");
        Long reservaId = Validaciones.requerido(command.reservaId(), "reservaId");
        ReservaFacturable reserva = AccesoFacturacion.cargarReserva(consultaReservas, reservaId);
        if (!reserva.clienteId().equals(command.solicitante().id())) {
            throw new AccesoDenegadoException("Solo puedes pagar tus propias reservas");
        }
        if (!reserva.estaAceptada()) {
            throw new ReservaNoPagableException(reservaId, reserva.estado());
        }
        if (pagoRepository.existePagoVigente(reservaId)) {
            throw new PagoDuplicadoException(reservaId);
        }

        LocalDateTime ahora = LocalDateTime.now(clock);
        return unidadDeTrabajo.ejecutar(() -> {
            Pago pago = Pago.registrar(reservaId, reserva.precioTotal(), metodo, nuevaReferencia(ahora), ahora);
            if (metodo.seApruebaAlInstante()) {
                pago.aprobar();
            }
            Pago guardado = pagoRepository.guardar(pago);
            Factura factura = guardado.estaAprobado() ? emisorFactura.emitir(guardado, reserva) : null;
            return PagoResponse.desde(guardado, factura);
        });
    }

    /** PAG-<año>-<12 hex>: única en la práctica; uq_pagos_referencia lo garantiza en la BD. */
    private static ReferenciaPago nuevaReferencia(LocalDateTime ahora) {
        String corto = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
        return new ReferenciaPago("PAG-" + ahora.getYear() + "-" + corto);
    }
}
