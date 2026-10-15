package com.rentaherramientas.domain.billing.model.aggregate;

import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;
import com.rentaherramientas.domain.shared.model.Validaciones;
import com.rentaherramientas.domain.shared.model.valueobject.Dinero;

/**
 * Entidad hija de Factura. El subtotal NO es un atributo: se calcula
 * (cantidad × valor unitario) para cumplir la normalización y no poder
 * desincronizarse nunca de sus factores.
 */
public final class LineaFactura {

    private static final int DESCRIPCION_MAX = 255;

    private final Long id;
    private final String descripcion;
    private final int cantidad;
    private final Dinero valorUnitario;

    private LineaFactura(Long id, String descripcion, int cantidad, Dinero valorUnitario) {
        if (cantidad < 1) {
            throw new ValorInvalidoException("La cantidad de la línea debe ser al menos 1");
        }
        this.id = id;
        this.descripcion = Validaciones.textoRequerido(descripcion, "descripcion", DESCRIPCION_MAX);
        this.cantidad = cantidad;
        this.valorUnitario = Validaciones.requerido(valorUnitario, "valorUnitario");
    }

    public static LineaFactura nueva(String descripcion, int cantidad, Dinero valorUnitario) {
        return new LineaFactura(null, descripcion, cantidad, valorUnitario);
    }

    public static LineaFactura reconstruir(Long id, String descripcion, int cantidad, Dinero valorUnitario) {
        return new LineaFactura(Validaciones.requerido(id, "id"), descripcion, cantidad, valorUnitario);
    }

    public Dinero subtotal() {
        return valorUnitario.multiplicar(cantidad);
    }

    public Long getId() {
        return id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getCantidad() {
        return cantidad;
    }

    public Dinero getValorUnitario() {
        return valorUnitario;
    }
}
