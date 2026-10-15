package com.rentaherramientas.domain.catalog.port.repository;

import com.rentaherramientas.domain.catalog.model.aggregate.Herramienta;
import com.rentaherramientas.domain.catalog.model.valueobject.EstadoHerramienta;
import com.rentaherramientas.domain.shared.model.Pagina;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida del agregado Herramienta. Siempre devuelve el agregado completo
 * (con imágenes y categorías) para que sus invariantes puedan verificarse.
 */
public interface HerramientaRepository {

    Herramienta guardar(Herramienta herramienta);

    Optional<Herramienta> buscarPorId(Long id);

    List<Herramienta> buscarPorProveedorId(Long proveedorId);

    /** Solo los ids de las herramientas del proveedor (sin cargar imágenes ni categorías). */
    List<Long> idsPorProveedor(Long proveedorId);

    List<Herramienta> buscarPorEstado(EstadoHerramienta estado);

    /** Búsqueda paginada con filtros opcionales, ordenada por id. */
    Pagina<Herramienta> buscar(CriteriosBusquedaHerramienta criterios);
}
