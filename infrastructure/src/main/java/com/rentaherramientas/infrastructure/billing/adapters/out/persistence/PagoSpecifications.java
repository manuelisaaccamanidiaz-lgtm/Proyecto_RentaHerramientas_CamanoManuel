package com.rentaherramientas.infrastructure.billing.adapters.out.persistence;

import com.rentaherramientas.domain.billing.port.repository.CriteriosBusquedaPago;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Traduce los criterios de búsqueda de pagos a Specifications de Spring Data.
 * Cada filtro solo se añade si viene informado.
 */
final class PagoSpecifications {

    private PagoSpecifications() {
    }

    static Specification<PagoEntity> segun(CriteriosBusquedaPago criterios) {
        List<Specification<PagoEntity>> filtros = new ArrayList<>();
        if (criterios.estado() != null) {
            filtros.add((root, query, cb) -> cb.equal(root.get("estado"), criterios.estado()));
        }
        if (criterios.metodo() != null) {
            filtros.add((root, query, cb) -> cb.equal(root.get("metodo"), criterios.metodo()));
        }
        if (criterios.reservaIds() != null) {
            filtros.add(deReservas(criterios.reservaIds()));
        }
        if (criterios.desde() != null) {
            LocalDateTime inicio = criterios.desde().atStartOfDay();
            filtros.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("fecha"), inicio));
        }
        if (criterios.hasta() != null) {
            // Día "hasta" completo: estrictamente antes de la medianoche siguiente.
            LocalDateTime finExclusivo = criterios.hasta().plusDays(1).atStartOfDay();
            filtros.add((root, query, cb) -> cb.lessThan(root.get("fecha"), finExclusivo));
        }
        return Specification.allOf(filtros);
    }

    /** Un IN vacío no es SQL válido: sin reservas, la condición es siempre falsa. */
    private static Specification<PagoEntity> deReservas(Collection<Long> ids) {
        return (root, query, cb) -> ids.isEmpty() ? cb.disjunction() : root.get("reservaId").in(ids);
    }
}
