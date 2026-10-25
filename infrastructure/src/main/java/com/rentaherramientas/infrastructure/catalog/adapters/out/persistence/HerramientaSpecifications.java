package com.rentaherramientas.infrastructure.catalog.adapters.out.persistence;

import com.rentaherramientas.domain.catalog.model.valueobject.EstadoHerramienta;
import com.rentaherramientas.domain.catalog.port.repository.CriteriosBusquedaHerramienta;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Traduce los criterios de búsqueda del dominio a Specifications de Spring Data.
 * Cada filtro solo se añade si viene informado. Solo filtran: ninguna hace fetch
 * de colecciones, porque un fetch join con paginación obliga a paginar en memoria.
 *
 * TÉCNICA AVANZADA (Spring Data JPA): Specifications (Criteria API)
 *   Qué hace: una `Specification` es una lambda `(root, query, cb) -> Predicate` que describe una condición
 *     WHERE. `root` es la entidad, `cb` (CriteriaBuilder) crea condiciones (`equal`, `like`, `or`...). Se
 *     combinan con `Specification.allOf(...)` (AND) o `Specification.not(...)`, lo que permite armar filtros
 *     opcionales sin concatenar SQL a mano.
 *   Cómo usarlo: `jpaRepository.findAll(spec, pageable)` en un repositorio que extienda
 *     `JpaSpecificationExecutor`.
 */
final class HerramientaSpecifications {

    private static final char ESCAPE = '\\';

    /** Constructor privado: clase de utilidades estáticas. */
    private HerramientaSpecifications() {
    }

    /** Combina con AND solo los filtros informados; sin estado, excluye las INACTIVAS si se pide. */
    static Specification<HerramientaEntity> segun(CriteriosBusquedaHerramienta criterios) {
        List<Specification<HerramientaEntity>> filtros = new ArrayList<>();
        if (criterios.categoriaId() != null) {
            filtros.add(enCategoria(criterios.categoriaId()));
        }
        if (criterios.estado() != null) {
            filtros.add(conEstado(criterios.estado()));
        } else if (criterios.excluirInactivas()) {
            filtros.add(Specification.not(conEstado(EstadoHerramienta.INACTIVA)));
        }
        if (criterios.texto() != null) {
            filtros.add(contieneTexto(criterios.texto()));
        }
         if (criterios.marca() != null) {
            filtros.add(deMarca(criterios.marca()));
        }
         if (criterios.precioMin() != null) {
            filtros.add(tarifaDesde(criterios.precioMin()));
        }
        if (criterios.precioMax() != null) {
            filtros.add(tarifaHasta(criterios.precioMax()));
        }
        if (criterios.proveedorId() != null) {
            filtros.add(deProveedor(criterios.proveedorId()));
        }
        return Specification.allOf(filtros);
    }

    /** Join con la tabla puente: como se filtra por UNA categoría, no duplica filas. */
    private static Specification<HerramientaEntity> enCategoria(Long categoriaId) {
        return (root, query, cb) -> cb.equal(root.join("categoriaIds"), categoriaId);
    }

    /** Filtro: herramientas en el estado indicado. */
    private static Specification<HerramientaEntity> conEstado(EstadoHerramienta estado) {
        return (root, query, cb) -> cb.equal(root.get("estado"), estado);
    }

    /** Filtro: herramientas del proveedor indicado. */
    private static Specification<HerramientaEntity> deProveedor(Long proveedorId) {
        return (root, query, cb) -> cb.equal(root.get("proveedorId"), proveedorId);
    }

    private static Specification<HerramientaEntity> deMarca(String marca){
        String patron = "%" + escaparLike(marca.toLowerCase(Locale.ROOT)) + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("marca")), patron , ESCAPE);
    }

    private static Specification<HerramientaEntity> tarifaDesde(BigDecimal min){
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("tarifaDiaria"), min);
    }

    private static Specification<HerramientaEntity> tarifaHasta(BigDecimal max){
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("tarifaDiaria"), max);
    }

    /**
     * Nombre o descripción contienen el texto, sin distinguir mayúsculas; % y _ se buscan literalmente.
     *
     * Nota: `cb.like(campo, patron, ESCAPE)` indica qué carácter escapa los comodines de LIKE.
     */
    private static Specification<HerramientaEntity> contieneTexto(String texto) {
        String patron = "%" + escaparLike(texto.toLowerCase(Locale.ROOT)) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nombre")), patron, ESCAPE),
                cb.like(cb.lower(root.get("descripcion")), patron, ESCAPE));
    }

    /** Escapa los comodines de LIKE (% y _) y la propia barra invertida. */
    private static String escaparLike(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
