package com.rentaherramientas.domain.rental.model.aggregate;

import java.time.LocalDateTime;

import com.rentaherramientas.domain.shared.model.Validaciones;
import com.rentaherramientas.domain.shared.model.valueobject.Dinero;

/**
 * Entidad hija de Reserva. Solo se crea a través de {@link Reserva#reportarDanio},
 * que es quien verifica que la reserva esté DEVUELTA. El costo es opcional (se
 * puede reportar el daño antes de cotizarlo) y va en la moneda de la reserva.
 */
public final class ReporteDanio {

    private static final int DESCRIPCION_MAX = 2000;

    private final Long id;
    private final String descripcion;
    private final Dinero costoEstimado;
    private final LocalDateTime reportadoEn;

    /** Constructor privado: exige descripción y fecha; el costo estimado es opcional. */
    private ReporteDanio(Long id, String descripcion, Dinero costoEstimado, LocalDateTime reportadoEn) {
        this.id = id;
        this.descripcion = Validaciones.textoRequerido(descripcion, "descripcion", DESCRIPCION_MAX);
        this.costoEstimado = costoEstimado;
        this.reportadoEn = Validaciones.requerido(reportadoEn, "reportadoEn");
    }

    /** Crea un reporte nuevo sin id. Visibilidad de paquete: solo la Reserva puede crearlo. */
    static ReporteDanio nuevo(String descripcion, Dinero costoEstimado, LocalDateTime reportadoEn) {
        return new ReporteDanio(null, descripcion, costoEstimado, reportadoEn);
    }

    /** Reconstruye un reporte leído de la base de datos. */
    public static ReporteDanio reconstruir(Long id, String descripcion, Dinero costoEstimado,
                                           LocalDateTime reportadoEn) {
        return new ReporteDanio(Validaciones.requerido(id, "id"), descripcion, costoEstimado, reportadoEn);
    }

    /** Identificador del reporte; null si aún no se ha guardado. */
    public Long getId() {
        return id;
    }

    /** Descripción del daño observado. */
    public String getDescripcion() {
        return descripcion;
    }

    /** Puede ser null si el daño aún no se ha cotizado. */
    public Dinero getCostoEstimado() {
        return costoEstimado;
    }

    /** Momento en que se reportó el daño. */
    public LocalDateTime getReportadoEn() {
        return reportadoEn;
    }
}
