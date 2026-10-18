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

    /** Constructor privado: guarda una copia inmutable de las líneas y las valida. */
    private Factura(Long id, NumeroFactura numero, Long pagoId,
                    LocalDateTime fechaEmision, List<LineaFactura> lineas) {
        this.id = id;
        this.numero = Validaciones.requerido(numero, "numero");
        this.pagoId = Validaciones.requerido(pagoId, "pagoId");
        this.fechaEmision = Validaciones.requerido(fechaEmision, "fechaEmision");
        this.lineas = List.copyOf(Validaciones.requerido(lineas, "lineas"));
        validarLineas(this.lineas);
    }

    /** Emite una factura nueva para un pago y registra el evento FacturaEmitida. */
    public static Factura emitir(NumeroFactura numero, Long pagoId,
                                 LocalDateTime fechaEmision, List<LineaFactura> lineas) {
        Factura factura = new Factura(null, numero, pagoId, fechaEmision, lineas);
        factura.registrarEvento(new FacturaEmitida(numero.valor(), pagoId, LocalDateTime.now()));
        return factura;
    }

    /** Reconstruye una factura leída de la base de datos; no emite eventos. */
    public static Factura reconstruir(Long id, NumeroFactura numero, Long pagoId,
                                      LocalDateTime fechaEmision, List<LineaFactura> lineas) {
        return new Factura(Validaciones.requerido(id, "id"), numero, pagoId, fechaEmision, lineas);
    }

    /**
     * Exige al menos una línea y que todas estén en la misma moneda.
     *
     * TÉCNICA AVANZADA (Java 21): `List.getFirst()`
     *   Qué hace: método de las "sequenced collections" que reemplaza a `lista.get(0)`. `stream().anyMatch`
     *     recorre la lista y se detiene en el primer elemento que cumple la condición.
     */
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

    /** Identificador de la factura; null si aún no se ha guardado. */
    public Long getId() {
        return id;
    }

    /** Número legal y único de la factura. */
    public NumeroFactura getNumero() {
        return numero;
    }

    /** Id del pago aprobado que originó la factura. */
    public Long getPagoId() {
        return pagoId;
    }

    /** Momento de emisión de la factura. */
    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    /** Líneas de la factura; la lista es inmutable. */
    public List<LineaFactura> getLineas() {
        return lineas;
    }
}
