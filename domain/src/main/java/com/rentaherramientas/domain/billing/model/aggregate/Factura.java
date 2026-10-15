package com.rentaherramientas.domain.billing.model.aggregate;

import com.rentaherramientas.domain.billing.event.FacturaEmitida;
import com.rentaherramientas.domain.billing.exception.FacturaInvalidaException;
import com.rentaherramientas.domain.billing.model.valueobject.NumeroFactura;
import com.rentaherramientas.domain.shared.model.Validaciones;
import com.rentaherramientas.domain.shared.model.aggregate.AggregateRoot;
import com.rentaherramientas.domain.shared.model.valueobject.Dinero;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Raíz del agregado Factura. Una factura emitida es un documento legal inmutable:
 * no tiene métodos de modificación. El total se calcula a partir de las líneas y
 * nunca se almacena. La reserva no se guarda aquí: se obtiene a través del pago
 * (guardarla sería una dependencia transitiva, contraria a 3FN). Que el pago esté
 * APROBADO lo verifica el caso de uso, porque Pago es otro agregado.
 */
public final class Factura extends AggregateRoot {

    private final Long id;
    private final NumeroFactura numero;
    private final Long pagoId;
    private final LocalDateTime fechaEmision;
    private final List<LineaFactura> lineas;

    private Factura(Long id, NumeroFactura numero, Long pagoId,
                    LocalDateTime fechaEmision, List<LineaFactura> lineas) {
        this.id = id;
        this.numero = Validaciones.requerido(numero, "numero");
        this.pagoId = Validaciones.requerido(pagoId, "pagoId");
        this.fechaEmision = Validaciones.requerido(fechaEmision, "fechaEmision");
        this.lineas = List.copyOf(Validaciones.requerido(lineas, "lineas"));
        validarLineas(this.lineas);
    }

    public static Factura emitir(NumeroFactura numero, Long pagoId,
                                 LocalDateTime fechaEmision, List<LineaFactura> lineas) {
        Factura factura = new Factura(null, numero, pagoId, fechaEmision, lineas);
        factura.registrarEvento(new FacturaEmitida(numero.valor(), pagoId, LocalDateTime.now()));
        return factura;
    }

    public static Factura reconstruir(Long id, NumeroFactura numero, Long pagoId,
                                      LocalDateTime fechaEmision, List<LineaFactura> lineas) {
        return new Factura(Validaciones.requerido(id, "id"), numero, pagoId, fechaEmision, lineas);
    }

    private static void validarLineas(List<LineaFactura> lineas) {
        if (lineas.isEmpty()) {
            throw new FacturaInvalidaException("La factura debe tener al menos una línea");
        }
        String moneda = lineas.getFirst().getValorUnitario().moneda();
        boolean monedasMezcladas = lineas.stream()
                .anyMatch(linea -> !linea.getValorUnitario().moneda().equals(moneda));
        if (monedasMezcladas) {
            throw new FacturaInvalidaException("Todas las líneas de la factura deben estar en la misma moneda");
        }
    }

    /** Moneda de la factura: la de sus líneas (todas iguales por invariante). */
    public String getMoneda() {
        return lineas.getFirst().getValorUnitario().moneda();
    }

    /** Total calculado; no existe columna total en la BD. */
    public Dinero total() {
        return lineas.stream()
                .map(LineaFactura::subtotal)
                .reduce(Dinero.cero(getMoneda()), Dinero::sumar);
    }

    public Long getId() {
        return id;
    }

    public NumeroFactura getNumero() {
        return numero;
    }

    public Long getPagoId() {
        return pagoId;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public List<LineaFactura> getLineas() {
        return lineas;
    }
}
