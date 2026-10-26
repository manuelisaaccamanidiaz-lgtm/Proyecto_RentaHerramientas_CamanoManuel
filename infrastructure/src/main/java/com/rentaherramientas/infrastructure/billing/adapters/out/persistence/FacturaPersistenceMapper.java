package com.rentaherramientas.infrastructure.billing.adapters.out.persistence;

import com.rentaherramientas.domain.billing.model.aggregate.Factura;
import com.rentaherramientas.domain.billing.model.aggregate.LineaFactura;
import com.rentaherramientas.domain.billing.model.valueobject.NumeroFactura;
import com.rentaherramientas.domain.shared.model.valueobject.Dinero;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Traduce el agregado Factura (raíz + líneas) hacia/desde JPA. La moneda se
 * guarda una sola vez en facturas.moneda y al leer se reparte a cada línea para
 * reconstruir su VO Dinero; el total nunca se persiste, lo calcula el dominio.
 */
@Component
public class FacturaPersistenceMapper {

    /** Convierte la entidad (con sus líneas) en el agregado Factura. */
    public Factura toDomain(FacturaEntity entity) {
        List<LineaFactura> lineas = entity.getLineas().stream()
                .map(linea -> LineaFactura.reconstruir(
                        linea.getId(),
                        linea.getDescripcion(),
                        linea.getCantidad(),
                        Dinero.de(linea.getValorUnitario(), entity.getMoneda())))
                .toList();
        return Factura.reconstruir(
                entity.getId(),
                new NumeroFactura(entity.getNumero()),
                entity.getPagoId(),
                entity.getFechaEmision(),
                lineas);
    }

    /** Convierte el agregado en entidad y añade sus líneas. */
    public FacturaEntity toEntity(Factura factura) {
        FacturaEntity entity = new FacturaEntity(
                factura.getId(),
                factura.getNumero().valor(),
                factura.getPagoId(),
                factura.getMoneda(),
                factura.getFechaEmision());
        factura.getLineas().forEach(linea -> entity.agregarLinea(new FacturaLineaEntity(
                linea.getId(),
                linea.getDescripcion(),
                linea.getCantidad(),
                linea.getValorUnitario().monto())));
        return entity;
    }
}
