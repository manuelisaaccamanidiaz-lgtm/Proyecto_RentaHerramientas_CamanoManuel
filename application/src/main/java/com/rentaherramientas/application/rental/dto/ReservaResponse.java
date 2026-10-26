package com.rentaherramientas.application.rental.dto;

import com.rentaherramientas.domain.rental.model.aggregate.Reserva;
import com.rentaherramientas.domain.rental.model.valueobject.EstadoReserva;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Vista de una reserva con sus reportes de daño. Los montos van en la moneda de
 * la reserva. pagada indica si tiene un pago APROBADO (lo informa billing).
 */
public record ReservaResponse(Long id, Long herramientaId, Long clienteId, LocalDate fechaInicio,
                              LocalDate fechaFin, long dias, EstadoReserva estado, BigDecimal precioTotal,
                              String moneda, boolean pagada, LocalDateTime fechaEntrega,
                              LocalDateTime fechaDevolucion, LocalDateTime creadoEn,
                              List<ReporteDanioResponse> reportesDanio) {

    public record ReporteDanioResponse(Long id, String descripcion, BigDecimal costoEstimado,
                                       LocalDateTime reportadoEn) {
    }

    public static ReservaResponse desde(Reserva reserva, boolean pagada) {
        return new ReservaResponse(
                reserva.getId(),
                reserva.getHerramientaId(),
                reserva.getClienteId(),
                reserva.getRango().inicio(),
                reserva.getRango().fin(),
                reserva.getRango().dias(),
                reserva.getEstado(),
                reserva.getPrecioTotal().monto(),
                reserva.getPrecioTotal().moneda(),
                pagada,
                reserva.getFechaEntrega(),
                reserva.getFechaDevolucion(),
                reserva.getCreadoEn(),
                reserva.getReportes().stream()
                        .map(reporte -> new ReporteDanioResponse(
                                reporte.getId(),
                                reporte.getDescripcion(),
                                reporte.getCostoEstimado() == null ? null : reporte.getCostoEstimado().monto(),
                                reporte.getReportadoEn()))
                        .toList());
    }
}
