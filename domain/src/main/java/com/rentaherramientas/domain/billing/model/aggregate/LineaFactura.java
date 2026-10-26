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

    /** Constructor privado: exige cantidad mínima de 1, descripción y valor unitario. */
    private LineaFactura(Long id, String descripcion, int cantidad, Dinero valorUnitario) {
        if (cantidad < 1) {
            throw new ValorInvalidoException("La cantidad de la línea debe ser al menos 1");
        }
        this.id = id;
        this.descripcion = Validaciones.textoRequerido(descripcion, "descripcion", DESCRIPCION_MAX);
        this.cantidad = cantidad;
        this.valorUnitario = Validaciones.requerido(valorUnitario, "valorUnitario");
    }

    /** Crea una línea nueva sin id. */
    public static LineaFactura nueva(String descripcion, int cantidad, Dinero valorUnitario) {
        return new LineaFactura(null, descripcion, cantidad, valorUnitario);
    }

    /** Reconstruye una línea leída de la base de datos. */
    public static LineaFactura reconstruir(Long id, String descripcion, int cantidad, Dinero valorUnitario) {
        return new LineaFactura(Validaciones.requerido(id, "id"), descripcion, cantidad, valorUnitario);
    }

    /** Subtotal de la línea: valor unitario × cantidad. */
    public Dinero subtotal() {
        return valorUnitario.multiplicar(cantidad);
    }

    /** Identificador de la línea; null si aún no se ha guardado. */
    public Long getId() {
        return id;
    }

    /** Texto descriptivo de la línea. */
    public String getDescripcion() {
        return descripcion;
    }

    /** Cantidad facturada (en el alquiler, número de días). */
    public int getCantidad() {
        return cantidad;
    }

    /** Precio por unidad (en el alquiler, la tarifa diaria). */
    public Dinero getValorUnitario() {
        return valorUnitario;
    }
}
