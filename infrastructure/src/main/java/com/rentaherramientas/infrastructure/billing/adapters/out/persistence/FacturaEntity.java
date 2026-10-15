package com.rentaherramientas.infrastructure.billing.adapters.out.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de persistencia de "facturas". pago_id es otro agregado (Pago): Long
 * simple, sin @ManyToOne. No hay reserva_id: la reserva se obtiene vía el pago.
 * Las líneas son hijas del agregado: cascada desde el padre. No hay columna total.
 */
@Entity
@Table(name = "facturas")
public class FacturaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "numero", nullable = false, length = 30, unique = true)
    private String numero;

    @Column(name = "pago_id", nullable = false, unique = true)
    private Long pagoId;

    /** CHAR(3) en PostgreSQL: se declara el tipo JDBC para que ddl-auto=validate lo acepte. */
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "moneda", nullable = false, length = 3)
    private String moneda;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<FacturaLineaEntity> lineas = new ArrayList<>();

    protected FacturaEntity() {
        // requerido por JPA
    }

    public FacturaEntity(Long id, String numero, Long pagoId, String moneda, LocalDateTime fechaEmision) {
        this.id = id;
        this.numero = numero;
        this.pagoId = pagoId;
        this.moneda = moneda;
        this.fechaEmision = fechaEmision;
    }

    /** Mantiene sincronizados ambos lados de la relación padre-hijo. */
    public void agregarLinea(FacturaLineaEntity linea) {
        linea.setFactura(this);
        lineas.add(linea);
    }

    public Long getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public Long getPagoId() {
        return pagoId;
    }

    public String getMoneda() {
        return moneda;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public List<FacturaLineaEntity> getLineas() {
        return lineas;
    }
}
