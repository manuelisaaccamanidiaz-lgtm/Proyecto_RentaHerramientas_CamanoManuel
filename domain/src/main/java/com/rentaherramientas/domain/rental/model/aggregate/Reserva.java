package com.rentaherramientas.domain.rental.model.aggregate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.rentaherramientas.domain.rental.event.DanioReportado;
import com.rentaherramientas.domain.rental.event.ReservaAceptada;
import com.rentaherramientas.domain.rental.event.ReservaCancelada;
import com.rentaherramientas.domain.rental.event.ReservaDevuelta;
import com.rentaherramientas.domain.rental.event.ReservaEntregada;
import com.rentaherramientas.domain.rental.event.ReservaRechazada;
import com.rentaherramientas.domain.rental.event.ReservaSolicitada;
import com.rentaherramientas.domain.rental.exception.DanioNoPermitidoException;
import com.rentaherramientas.domain.rental.exception.FechaInvalidaException;
import com.rentaherramientas.domain.rental.exception.TransicionDeEstadoInvalidaException;
import com.rentaherramientas.domain.rental.model.valueobject.EstadoReserva;
import com.rentaherramientas.domain.rental.model.valueobject.RangoFechas;
import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;
import com.rentaherramientas.domain.shared.model.Validaciones;
import com.rentaherramientas.domain.shared.model.aggregate.AggregateRoot;
import com.rentaherramientas.domain.shared.model.valueobject.Dinero;

/**
 * Raíz del agregado Reserva. Protege la máquina de estados, la coherencia entre
 * estado y fechas de entrega/devolución, y que los daños solo se reporten tras
 * la devolución. La herramienta (catalog) y el cliente (identity) se referencian
 * solo por id. El precio total es una foto del momento de la reserva (ver
 * NORMALIZACION §3.3), por eso se calcula una vez y no se recalcula. Que no se
 * solape con otras reservas lo verifica el caso de uso, porque involucra varios
 * agregados.
 */
public final class Reserva extends AggregateRoot {

    private final Long id;
    private final Long herramientaId;
    private final Long clienteId;
    private final RangoFechas rango;
    private EstadoReserva estado;
    private final Dinero precioTotal;
    private LocalDateTime fechaEntrega;
    private LocalDateTime fechaDevolucion;
    private final LocalDateTime creadoEn;
    private final List<ReporteDanio> reportes;

    /** Constructor privado: valida datos obligatorios y que el precio total sea positivo. */
    private Reserva(Long id, Long herramientaId, Long clienteId, RangoFechas rango, EstadoReserva estado,
                    Dinero precioTotal, LocalDateTime fechaEntrega, LocalDateTime fechaDevolucion,
                    LocalDateTime creadoEn, List<ReporteDanio> reportes) {
        this.id = id;
        this.herramientaId = Validaciones.requerido(herramientaId, "herramientaId");
        this.clienteId = Validaciones.requerido(clienteId, "clienteId");
        this.rango = Validaciones.requerido(rango, "rango");
        this.estado = Validaciones.requerido(estado, "estado");
        this.precioTotal = Validaciones.requerido(precioTotal, "precioTotal");
        if (!precioTotal.esPositivo()) {
            throw new ValorInvalidoException("El precio total de la reserva debe ser positivo");
        }
        this.fechaEntrega = fechaEntrega;
        this.fechaDevolucion = fechaDevolucion;
        this.creadoEn = Validaciones.requerido(creadoEn, "creadoEn");
        this.reportes = new ArrayList<>(Validaciones.requerido(reportes, "reportes"));
    }

    /** Alta de una reserva: empieza PENDIENTE y su precio es tarifa diaria × días. */
    public static Reserva registrar(Long herramientaId, Long clienteId, RangoFechas rango, Dinero tarifaDiaria) {
        Validaciones.requerido(rango, "rango");
        Validaciones.requerido(tarifaDiaria, "tarifaDiaria");
        Dinero precioTotal = tarifaDiaria.multiplicar(Math.toIntExact(rango.dias()));
        LocalDateTime ahora = LocalDateTime.now();
        Reserva reserva = new Reserva(null, herramientaId, clienteId, rango, EstadoReserva.PENDIENTE,
                precioTotal, null, null, ahora, List.of());
        reserva.registrarEvento(new ReservaSolicitada(herramientaId, clienteId, rango, ahora));
        return reserva;
    }

    /** Reconstruye una reserva leída de la base de datos; no emite eventos. */
    public static Reserva reconstruir(Long id, Long herramientaId, Long clienteId, RangoFechas rango,
                                      EstadoReserva estado, Dinero precioTotal, LocalDateTime fechaEntrega,
                                      LocalDateTime fechaDevolucion, LocalDateTime creadoEn,
                                      List<ReporteDanio> reportes) {
        return new Reserva(Validaciones.requerido(id, "id"), herramientaId, clienteId, rango, estado,
                precioTotal, fechaEntrega, fechaDevolucion, creadoEn, reportes);
    }

    // -------------------------------------------------------------- estados

    /** El proveedor acepta la solicitud: PENDIENTE → ACEPTADA; emite ReservaAceptada. */
    public void aceptar() {
        cambiarEstado(EstadoReserva.ACEPTADA);
        registrarEvento(new ReservaAceptada(id, herramientaId, clienteId, LocalDateTime.now()));
    }

    /** El proveedor rechaza la solicitud: PENDIENTE → RECHAZADA; emite ReservaRechazada. */
    public void rechazar() {
        cambiarEstado(EstadoReserva.RECHAZADA);
        registrarEvento(new ReservaRechazada(id, herramientaId, clienteId, LocalDateTime.now()));
    }

    /** El cliente cancela su reserva → CANCELADA; emite ReservaCancelada. */
    public void cancelar() {
        cambiarEstado(EstadoReserva.CANCELADA);
        registrarEvento(new ReservaCancelada(id, herramientaId, clienteId, LocalDateTime.now()));
    }

    /** Marca la reserva como VENCIDA cuando pasó su fecha sin entregarse (lo hace la tarea programada). */
    public void vencer() {
        cambiarEstado(EstadoReserva.VENCIDA);
    }

    /** Registra que la herramienta se entregó al cliente: ACEPTADA → ENTREGADA; emite ReservaEntregada. */
    public void registrarEntrega(LocalDateTime fechaEntrega) {
        Validaciones.requerido(fechaEntrega, "fechaEntrega");
        cambiarEstado(EstadoReserva.ENTREGADA);
        this.fechaEntrega = fechaEntrega;
        registrarEvento(new ReservaEntregada(id, herramientaId, clienteId, LocalDateTime.now()));
    }

    /**
     * Registra la devolución (ENTREGADA → DEVUELTA), valida que no sea anterior a la entrega
     * y emite el evento ReservaDevuelta.
     */
    public void registrarDevolucion(LocalDateTime fechaDevolucion) {
        Validaciones.requerido(fechaDevolucion, "fechaDevolucion");
        if (fechaEntrega != null && fechaDevolucion.isBefore(fechaEntrega)) {
            throw new FechaInvalidaException("La devolución no puede ser anterior a la entrega");
        }
        cambiarEstado(EstadoReserva.DEVUELTA);
        this.fechaDevolucion = fechaDevolucion;
        registrarEvento(new ReservaDevuelta(id, herramientaId, LocalDateTime.now()));
    }

    /** Punto único de cambio de estado: rechaza transiciones no permitidas por EstadoReserva. */
    private void cambiarEstado(EstadoReserva destino) {
        if (!estado.puedeCambiarA(destino)) {
            throw new TransicionDeEstadoInvalidaException(estado, destino);
        }
        this.estado = destino;
    }

    // --------------------------------------------------------------- daños

    /** Solo tras la devolución; el costo (opcional) debe ir en la moneda de la reserva. */
    public void reportarDanio(String descripcion, Dinero costoEstimado) {
        if (estado != EstadoReserva.DEVUELTA) {
            throw new DanioNoPermitidoException(estado);
        }
        if (costoEstimado != null && !costoEstimado.mismaMonedaQue(precioTotal)) {
            throw new ValorInvalidoException(
                    "El costo del daño debe estar en la moneda de la reserva (" + precioTotal.moneda() + ")");
        }
        LocalDateTime ahora = LocalDateTime.now();
        reportes.add(ReporteDanio.nuevo(descripcion, costoEstimado, ahora));
        registrarEvento(new DanioReportado(id, herramientaId, costoEstimado, ahora));
    }

    // -------------------------------------------------------------- lectura

    /** Indica si las fechas de esta reserva se solapan con el rango dado. */
    public boolean seCruzaCon(RangoFechas otro) {
        return rango.seSolapaCon(otro);
    }

    /** Identificador de la reserva; null si aún no se ha guardado. */
    public Long getId() {
        return id;
    }

    /** Id de la herramienta reservada. */
    public Long getHerramientaId() {
        return herramientaId;
    }

    /** Id del usuario CLIENTE que hizo la reserva. */
    public Long getClienteId() {
        return clienteId;
    }

    /** Fechas de inicio y fin (ambas incluidas) de la reserva. */
    public RangoFechas getRango() {
        return rango;
    }

    /** Estado actual de la reserva. */
    public EstadoReserva getEstado() {
        return estado;
    }

    /** Precio total calculado al crearla (días × tarifa diaria). */
    public Dinero getPrecioTotal() {
        return precioTotal;
    }

    /** Momento de la entrega real; null si aún no se entregó. */
    public LocalDateTime getFechaEntrega() {
        return fechaEntrega;
    }

    /** Momento de la devolución real; null si aún no se devolvió. */
    public LocalDateTime getFechaDevolucion() {
        return fechaDevolucion;
    }

    /** Momento en que se creó la solicitud de reserva. */
    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    /** Copia inmutable ordenada por fecha: nadie agrega reportes sin pasar por el agregado. */
    public List<ReporteDanio> getReportes() {
        return reportes.stream().sorted(Comparator.comparing(ReporteDanio::getReportadoEn)).toList();
    }
}
