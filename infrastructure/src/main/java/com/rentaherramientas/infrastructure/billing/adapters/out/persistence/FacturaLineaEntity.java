package com.rentaherramientas.infrastructure.billing.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Tabla "factura_lineas": entidad hija del agregado Factura. No guarda subtotal
 * ni moneda (la moneda depende de la factura, no de la línea). El @ManyToOne
 * hacia el padre no lleva cascada.
 */
@Entity
@Table(name = "factura_lineas")
public class FacturaLineaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "factura_id", nullable = false)
    private FacturaEntity factura;

    @Column(name = "descripcion", nullable = false, length = 255)
    private String descripcion;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "valor_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorUnitario;

    /** Constructor sin argumentos exigido por JPA. */
    protected FacturaLineaEntity() {
        // requerido por JPA
    }

    /** Crea la entidad; la factura padre se enlaza después con setFactura. */
    public FacturaLineaEntity(Long id, String descripcion, Integer cantidad, BigDecimal valorUnitario) {
        this.id = id;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.valorUnitario = valorUnitario;
    }

    /** Enlaza la línea con su factura; lo llama FacturaEntity.agregarLinea. */
    void setFactura(FacturaEntity factura) {
        this.factura = factura;
    }

    /** Id de la línea. */
    public Long getId() {
        return id;
    }

    /** Descripción de la línea. */
    public String getDescripcion() {
        return descripcion;
    }

    /** Cantidad facturada. */
    public Integer getCantidad() {
        return cantidad;
    }

    /** Valor por unidad. */
    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }
}
