package com.rentaherramientas.domain.catalog.port.repository;

import com.rentaherramientas.domain.catalog.model.aggregate.Herramienta;
import com.rentaherramientas.domain.catalog.model.valueobject.EstadoHerramienta;

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

    List<Herramienta> buscarPorEstado(EstadoHerramienta estado);
}
