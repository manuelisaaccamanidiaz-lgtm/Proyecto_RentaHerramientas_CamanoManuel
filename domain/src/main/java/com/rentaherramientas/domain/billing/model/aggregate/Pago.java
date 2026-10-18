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

    /** Constructor privado: valida todos los campos y que el monto sea mayor que cero. */
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

    /** Registra un pago nuevo; siempre empieza en estado PENDIENTE. */
    public static Pago registrar(Long reservaId, Dinero monto, MetodoPago metodo,
                                 ReferenciaPago referencia, LocalDateTime fecha) {
        return new Pago(null, reservaId, monto, metodo, EstadoPago.PENDIENTE, referencia, fecha);
    }

    /** Reconstruye un pago leído de la base de datos; no emite eventos. */
    public static Pago reconstruir(Long id, Long reservaId, Dinero monto, MetodoPago metodo,
                                   EstadoPago estado, ReferenciaPago referencia, LocalDateTime fecha) {
        return new Pago(Validaciones.requerido(id, "id"), reservaId, monto, metodo, estado, referencia, fecha);
    }

    /** Aprueba el pago pendiente y registra el evento PagoAprobado. */
    public void aprobar() {
        resolver(EstadoPago.APROBADO);
        registrarEvento(new PagoAprobado(id, reservaId, monto, LocalDateTime.now()));
    }

    /** Rechaza el pago pendiente y emite PagoRechazado. */
    public void rechazar() {
        resolver(EstadoPago.RECHAZADO);
        registrarEvento(new PagoRechazado(id, reservaId, LocalDateTime.now()));
    }

    /** Indica si el pago está APROBADO. */
    public boolean estaAprobado() {
        return estado == EstadoPago.APROBADO;
    }

    /** Cambia a un estado final; falla si el pago ya estaba aprobado o rechazado. */
    private void resolver(EstadoPago destino) {
        if (estado.esFinal()) {
            throw new PagoInvalidoException("El pago ya está " + estado + " y no puede pasar a " + destino);
        }
        this.estado = destino;
    }

    /** Identificador del pago; null si aún no se ha guardado. */
    public Long getId() {
        return id;
    }

    /** Id de la reserva que se paga. */
    public Long getReservaId() {
        return reservaId;
    }

    /** Monto pagado. */
    public Dinero getMonto() {
        return monto;
    }

    /** Medio de pago usado. */
    public MetodoPago getMetodo() {
        return metodo;
    }

    /** Estado actual del pago. */
    public EstadoPago getEstado() {
        return estado;
    }

    /** Referencia única de la transacción. */
    public ReferenciaPago getReferencia() {
        return referencia;
    }

    /** Momento en que se registró el pago. */
    public LocalDateTime getFecha() {
        return fecha;
    }
}
