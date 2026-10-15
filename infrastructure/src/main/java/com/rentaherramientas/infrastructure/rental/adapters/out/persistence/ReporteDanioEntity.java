package com.rentaherramientas.infrastructure.rental.adapters.out.persistence;

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
import java.time.LocalDateTime;

/**
 * Tabla "reportes_danio": entidad hija del agregado Reserva. No guarda moneda
 * (depende de la reserva, no del reporte). El @ManyToOne hacia el padre no
 * lleva cascada.
 */
@Entity
@Table(name = "reportes_danio")
public class ReporteDanioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reserva_id", nullable = false)
    private ReservaEntity reserva;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "costo_estimado", precision = 12, scale = 2)
    private BigDecimal costoEstimado;

    @Column(name = "reportado_en", nullable = false)
    private LocalDateTime reportadoEn;

    protected ReporteDanioEntity() {
        // requerido por JPA
    }

    public ReporteDanioEntity(Long id, String descripcion, BigDecimal costoEstimado, LocalDateTime reportadoEn) {
        this.id = id;
        this.descripcion = descripcion;
        this.costoEstimado = costoEstimado;
        this.reportadoEn = reportadoEn;
    }

    void setReserva(ReservaEntity reserva) {
        this.reserva = reserva;
    }

    public Long getId() {
        return id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getCostoEstimado() {
        return costoEstimado;
    }

    public LocalDateTime getReportadoEn() {
        return reportadoEn;
    }
}
