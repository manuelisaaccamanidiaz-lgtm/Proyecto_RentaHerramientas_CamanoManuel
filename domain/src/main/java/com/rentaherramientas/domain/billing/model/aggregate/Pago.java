package com.rentaherramientas.domain.billing.model.aggregate;

import com.rentaherramientas.domain.billing.event.PagoAprobado;
import com.rentaherramientas.domain.billing.event.PagoRechazado;
import com.rentaherramientas.domain.billing.exception.PagoInvalidoException;
import com.rentaherramientas.domain.billing.model.valueobject.EstadoPago;
import com.rentaherramientas.domain.billing.model.valueobject.MetodoPago;
import com.rentaherramientas.domain.billing.model.valueobject.ReferenciaPago;
import com.rentaherramientas.domain.shared.model.Validaciones;
import com.rentaherramientas.domain.shared.model.aggregate.AggregateRoot;
import com.rentaherramientas.domain.shared.model.valueobject.Dinero;

import java.time.LocalDateTime;

/**
 * Raíz del agregado Pago. La reserva (BC rental) se referencia solo por id.
 * Monto, método, referencia y fecha son inmutables una vez registrado el pago;
 * lo único que evoluciona es el estado, y solo una vez (PENDIENTE → final).
 */
public final class Pago extends AggregateRoot {

    private final Long id;
    private final Long reservaId;
    private final Dinero monto;
    private final MetodoPago metodo;
    private EstadoPago estado;
    private final ReferenciaPago referencia;
    private final LocalDateTime fecha;

    private Pago(Long id, Long reservaId, Dinero monto, MetodoPago metodo, EstadoPago estado,
                 ReferenciaPago referencia, LocalDateTime fecha) {
        this.id = id;
        this.reservaId = Validaciones.requerido(reservaId, "reservaId");
        this.monto = Validaciones.requerido(monto, "monto");
        if (!monto.esPositivo()) {
            throw new PagoInvalidoException("El monto del pago debe ser mayor que cero");
        }
        this.metodo = Validaciones.requerido(metodo, "metodo");
        this.estado = Validaciones.requerido(estado, "estado");
        this.referencia = Validaciones.requerido(referencia, "referencia");
        this.fecha = Validaciones.requerido(fecha, "fecha");
    }

    public static Pago registrar(Long reservaId, Dinero monto, MetodoPago metodo,
                                 ReferenciaPago referencia, LocalDateTime fecha) {
        return new Pago(null, reservaId, monto, metodo, EstadoPago.PENDIENTE, referencia, fecha);
    }

    public static Pago reconstruir(Long id, Long reservaId, Dinero monto, MetodoPago metodo,
                                   EstadoPago estado, ReferenciaPago referencia, LocalDateTime fecha) {
        return new Pago(Validaciones.requerido(id, "id"), reservaId, monto, metodo, estado, referencia, fecha);
    }

    public void aprobar() {
        resolver(EstadoPago.APROBADO);
        registrarEvento(new PagoAprobado(id, reservaId, monto, LocalDateTime.now()));
    }

    public void rechazar() {
        resolver(EstadoPago.RECHAZADO);
        registrarEvento(new PagoRechazado(id, reservaId, LocalDateTime.now()));
    }

    public boolean estaAprobado() {
        return estado == EstadoPago.APROBADO;
    }

    private void resolver(EstadoPago destino) {
        if (estado.esFinal()) {
            throw new PagoInvalidoException("El pago ya está " + estado + " y no puede pasar a " + destino);
        }
        this.estado = destino;
    }

    public Long getId() {
        return id;
    }

    public Long getReservaId() {
        return reservaId;
    }

    public Dinero getMonto() {
        return monto;
    }

    public MetodoPago getMetodo() {
        return metodo;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public ReferenciaPago getReferencia() {
        return referencia;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}
