package com.rentaherramientas.infrastructure.rental.adapters.out.persistence;

import com.rentaherramientas.domain.rental.model.valueobject.EstadoReserva;
import com.rentaherramientas.domain.rental.port.repository.CriteriosBusquedaReserva;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Traduce los criterios de búsqueda de reservas a Specifications. Solo filtran:
 * los reportes de daño se cargan aparte, en lote, para no paginar en memoria.
 */
final class ReservaSpecifications {

    private ReservaSpecifications() {
    }

    static Specification<ReservaEntity> segun(CriteriosBusquedaReserva criterios) {
        List<Specification<ReservaEntity>> filtros = new ArrayList<>();
        if (criterios.estado() != null) {
            filtros.add(conEstado(criterios.estado()));
        }
        if (criterios.clienteId() != null) {
            filtros.add(igual("clienteId", criterios.clienteId()));
        }
        if (criterios.herramientaId() != null) {
            filtros.add(igual("herramientaId", criterios.herramientaId()));
        }
        if (criterios.herramientaIds() != null) {
            filtros.add(deHerramientas(criterios.herramientaIds()));
        }
        if (criterios.desde() != null) {
            filtros.add(terminaDesde(criterios.desde()));
        }
        if (criterios.hasta() != null) {
            filtros.add(empiezaHasta(criterios.hasta()));
        }
        return Specification.allOf(filtros);
    }

    private static Specification<ReservaEntity> conEstado(EstadoReserva estado) {
        return (root, query, cb) -> cb.equal(root.get("estado"), estado);
    }

    private static Specification<ReservaEntity> igual(String atributo, Long valor) {
        return (root, query, cb) -> cb.equal(root.get(atributo), valor);
    }

    /** Un IN vacío no es SQL válido: sin herramientas, la condición es siempre falsa. */
    private static Specification<ReservaEntity> deHerramientas(Collection<Long> ids) {
        return (root, query, cb) -> ids.isEmpty() ? cb.disjunction() : root.get("herramientaId").in(ids);
    }

    /** Junto con empiezaHasta: el periodo de la reserva se cruza con [desde, hasta]. */
    private static Specification<ReservaEntity> terminaDesde(LocalDate desde) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("fechaFin"), desde);
    }

    private static Specification<ReservaEntity> empiezaHasta(LocalDate hasta) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("fechaInicio"), hasta);
    }
}
