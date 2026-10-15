package com.rentaherramientas.infrastructure.catalog.adapters.out.persistence;

import com.rentaherramientas.domain.catalog.model.valueobject.EstadoHerramienta;
import com.rentaherramientas.domain.catalog.port.repository.CriteriosBusquedaHerramienta;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Traduce los criterios de búsqueda del dominio a Specifications de Spring Data.
 * Cada filtro solo se añade si viene informado. Solo filtran: ninguna hace fetch
 * de colecciones, porque un fetch join con paginación obliga a paginar en memoria.
 */
final class HerramientaSpecifications {

    private static final char ESCAPE = '\\';

    private HerramientaSpecifications() {
    }

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
        if (criterios.proveedorId() != null) {
            filtros.add(deProveedor(criterios.proveedorId()));
        }
        return Specification.allOf(filtros);
    }

    /** Join con la tabla puente: como se filtra por UNA categoría, no duplica filas. */
    private static Specification<HerramientaEntity> enCategoria(Long categoriaId) {
        return (root, query, cb) -> cb.equal(root.join("categoriaIds"), categoriaId);
    }

    private static Specification<HerramientaEntity> conEstado(EstadoHerramienta estado) {
        return (root, query, cb) -> cb.equal(root.get("estado"), estado);
    }

    private static Specification<HerramientaEntity> deProveedor(Long proveedorId) {
        return (root, query, cb) -> cb.equal(root.get("proveedorId"), proveedorId);
    }

    /** Nombre o descripción contienen el texto, sin distinguir mayúsculas; % y _ se buscan literalmente. */
    private static Specification<HerramientaEntity> contieneTexto(String texto) {
        String patron = "%" + escaparLike(texto.toLowerCase(Locale.ROOT)) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nombre")), patron, ESCAPE),
                cb.like(cb.lower(root.get("descripcion")), patron, ESCAPE));
    }

    private static String escaparLike(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
