package com.rentaherramientas.infrastructure.rental.adapters.out.persistence;

import com.rentaherramientas.domain.rental.model.aggregate.ReporteDanio;
import com.rentaherramientas.domain.rental.model.aggregate.Reserva;
import com.rentaherramientas.domain.rental.model.valueobject.RangoFechas;
import com.rentaherramientas.domain.shared.model.valueobject.Dinero;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Traduce el agregado Reserva (raíz + reportes de daño) hacia/desde JPA. Aplana
 * RangoFechas en fecha_inicio/fecha_fin y Dinero en precio_total + moneda; la
 * moneda se guarda una sola vez en reservas y al leer se reparte a cada reporte.
 */
@Component
public class ReservaPersistenceMapper {

    /** Convierte la entidad (con sus reportes) en el agregado Reserva. */
    public Reserva toDomain(ReservaEntity entity) {
        List<ReporteDanio> reportes = entity.getReportes().stream()
                .map(reporte -> ReporteDanio.reconstruir(
                        reporte.getId(),
                        reporte.getDescripcion(),
                        reporte.getCostoEstimado() == null
                                ? null
                                : Dinero.de(reporte.getCostoEstimado(), entity.getMoneda()),
                        reporte.getReportadoEn()))
                .toList();
        return Reserva.reconstruir(
                entity.getId(),
                entity.getHerramientaId(),
                entity.getClienteId(),
                new RangoFechas(entity.getFechaInicio(), entity.getFechaFin()),
                entity.getEstado(),
                Dinero.de(entity.getPrecioTotal(), entity.getMoneda()),
                entity.getFechaEntrega(),
                entity.getFechaDevolucion(),
                entity.getCreadoEn(),
                reportes);
    }

    /** Convierte el agregado en entidad y añade sus reportes de daño. */
    public ReservaEntity toEntity(Reserva reserva) {
        ReservaEntity entity = new ReservaEntity(
                reserva.getId(),
                reserva.getHerramientaId(),
                reserva.getClienteId(),
                reserva.getRango().inicio(),
                reserva.getRango().fin(),
                reserva.getEstado(),
                reserva.getPrecioTotal().monto(),
                reserva.getPrecioTotal().moneda(),
                reserva.getFechaEntrega(),
                reserva.getFechaDevolucion(),
                reserva.getCreadoEn());
        reserva.getReportes().forEach(reporte -> entity.agregarReporte(new ReporteDanioEntity(
                reporte.getId(),
                reporte.getDescripcion(),
                reporte.getCostoEstimado() == null ? null : reporte.getCostoEstimado().monto(),
                reporte.getReportadoEn())));
        return entity;
    }
}
