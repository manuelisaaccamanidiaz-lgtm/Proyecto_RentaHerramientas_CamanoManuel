package com.rentaherramientas.infrastructure.billing.adapters.out.persistence;

import com.rentaherramientas.domain.billing.model.valueobject.EstadoPago;
import com.rentaherramientas.domain.billing.model.valueobject.MetodoPago;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Modelo de persistencia de "pagos". reserva_id es un Long simple: la reserva
 * pertenece al BC rental y entre BC solo se referencia por id.
 */
@Entity
@Table(name = "pagos")
public class PagoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "reserva_id", nullable = false)
    private Long reservaId;

    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    /** CHAR(3) en PostgreSQL: se declara el tipo JDBC para que ddl-auto=validate lo acepte. */
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "moneda", nullable = false, length = 3)
    private String moneda;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo", nullable = false, length = 20)
    private MetodoPago metodo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoPago estado;

    @Column(name = "referencia", nullable = false, length = 60, unique = true)
    private String referencia;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    protected PagoEntity() {
        // requerido por JPA
    }

    public PagoEntity(Long id, Long reservaId, BigDecimal monto, String moneda, MetodoPago metodo,
                      EstadoPago estado, String referencia, LocalDateTime fecha) {
        this.id = id;
        this.reservaId = reservaId;
        this.monto = monto;
        this.moneda = moneda;
        this.metodo = metodo;
        this.estado = estado;
        this.referencia = referencia;
        this.fecha = fecha;
    }

    public Long getId() {
        return id;
    }

    public Long getReservaId() {
        return reservaId;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public String getMoneda() {
        return moneda;
    }

    public MetodoPago getMetodo() {
        return metodo;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public String getReferencia() {
        return referencia;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}
