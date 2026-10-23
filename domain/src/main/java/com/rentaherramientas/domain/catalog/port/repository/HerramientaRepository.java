package com.rentaherramientas.domain.catalog.port.repository;

import com.rentaherramientas.domain.catalog.model.aggregate.Herramienta;
import com.rentaherramientas.domain.catalog.model.valueobject.EstadoHerramienta;
import com.rentaherramientas.domain.shared.model.Pagina;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida del agregado Herramienta. Siempre devuelve el agregado completo
 * (con imágenes y categorías) para que sus invariantes puedan verificarse.
 */
public interface HerramientaRepository {

    /** Inserta o actualiza la herramienta (con sus imágenes y categorías) y la devuelve con id. */
    Herramienta guardar(Herramienta herramienta);

    /** Busca una herramienta por id; vacío si no existe. */
    Optional<Herramienta> buscarPorId(Long id);

    /** Las herramientas con esos ids, en cualquier orden; los ids que no existen se ignoran. */
    List<Herramienta> buscarPorIds(Collection<Long> ids);

    /** Devuelve todas las herramientas de un proveedor. */
    List<Herramienta> buscarPorProveedorId(Long proveedorId);

    /** Solo los ids de las herramientas del proveedor (sin cargar imágenes ni categorías). */
    List<Long> idsPorProveedor(Long proveedorId);

    /** Devuelve todas las herramientas que están en el estado indicado. */
    List<Herramienta> buscarPorEstado(EstadoHerramienta estado);

    /** Búsqueda paginada con filtros opcionales, ordenada por id. */
    Pagina<Herramienta> buscar(CriteriosBusquedaHerramienta criterios);
}
