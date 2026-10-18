package com.rentaherramientas.infrastructure.billing.adapters.out.persistence;

import com.rentaherramientas.domain.billing.model.aggregate.Pago;
import com.rentaherramientas.domain.billing.model.valueobject.ReferenciaPago;
import com.rentaherramientas.domain.shared.model.valueobject.Dinero;
import org.springframework.stereotype.Component;

/**
 * Traduce entre el agregado Pago y PagoEntity; aplana Dinero en monto + moneda.
 */
@Component
public class PagoPersistenceMapper {

    /** Convierte la entidad en el agregado Pago. */
    public Pago toDomain(PagoEntity entity) {
        return Pago.reconstruir(
                entity.getId(),
                entity.getReservaId(),
                Dinero.de(entity.getMonto(), entity.getMoneda()),
                entity.getMetodo(),
                entity.getEstado(),
                new ReferenciaPago(entity.getReferencia()),
                entity.getFecha());
    }

    /** Convierte el agregado en entidad JPA. */
    public PagoEntity toEntity(Pago pago) {
        return new PagoEntity(
                pago.getId(),
                pago.getReservaId(),
                pago.getMonto().monto(),
                pago.getMonto().moneda(),
                pago.getMetodo(),
                pago.getEstado(),
                pago.getReferencia().valor(),
                pago.getFecha());
    }
}
