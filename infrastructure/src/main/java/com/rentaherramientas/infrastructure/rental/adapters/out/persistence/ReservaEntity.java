package com.rentaherramientas.infrastructure.rental.adapters.out.persistence;

import com.rentaherramientas.domain.rental.model.valueobject.EstadoReserva;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de persistencia de "reservas". herramienta_id (catalog) y cliente_id
 * (identity) son otros BC: Long simple, sin @ManyToOne. Los reportes de daño son
 * hijos del agregado: cascada desde el padre.
 */
@Entity
@Table(name = "reservas")
public class ReservaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "herramienta_id", nullable = false)
    private Long herramientaId;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoReserva estado;

    @Column(name = "precio_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioTotal;

    /** CHAR(3) en PostgreSQL: se declara el tipo JDBC para que ddl-auto=validate lo acepte. */
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "moneda", nullable = false, length = 3)
    private String moneda;

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;

    @Column(name = "fecha_devolucion")
    private LocalDateTime fechaDevolucion;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("reportadoEn ASC")
    private List<ReporteDanioEntity> reportes = new ArrayList<>();

    protected ReservaEntity() {
        // requerido por JPA
    }

    public ReservaEntity(Long id, Long herramientaId, Long clienteId, LocalDate fechaInicio, LocalDate fechaFin,
                         EstadoReserva estado, BigDecimal precioTotal, String moneda,
                         LocalDateTime fechaEntrega, LocalDateTime fechaDevolucion, LocalDateTime creadoEn) {
        this.id = id;
        this.herramientaId = herramientaId;
        this.clienteId = clienteId;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
        this.precioTotal = precioTotal;
        this.moneda = moneda;
        this.fechaEntrega = fechaEntrega;
        this.fechaDevolucion = fechaDevolucion;
        this.creadoEn = creadoEn;
    }

    /** Mantiene sincronizados ambos lados de la relación padre-hijo. */
    public void agregarReporte(ReporteDanioEntity reporte) {
        reporte.setReserva(this);
        reportes.add(reporte);
    }

    public Long getId() {
        return id;
    }

    public Long getHerramientaId() {
        return herramientaId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public BigDecimal getPrecioTotal() {
        return precioTotal;
    }

    public String getMoneda() {
        return moneda;
    }

    public LocalDateTime getFechaEntrega() {
        return fechaEntrega;
    }

    public LocalDateTime getFechaDevolucion() {
        return fechaDevolucion;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public List<ReporteDanioEntity> getReportes() {
        return reportes;
    }
}
