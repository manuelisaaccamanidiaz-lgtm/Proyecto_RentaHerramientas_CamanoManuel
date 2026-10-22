package com.rentaherramientas.application.catalog.dto;

import com.rentaherramientas.domain.catalog.model.aggregate.Herramienta;
import com.rentaherramientas.domain.catalog.model.valueobject.EstadoHerramienta;

import java.math.BigDecimal;
import java.util.List;

/**
 * Vista pública de una herramienta con su galería (ordenada) y los ids de sus categorías.
 */
public record HerramientaResponse(Long id, Long proveedorId, String nombre, String descripcion, String marca,
                                  BigDecimal tarifaDiaria, String moneda, EstadoHerramienta estado,
                                  List<Long> categoriaIds, List<ImagenResponse> imagenes) {

    public record ImagenResponse(int orden, String url) {
    }

    public static HerramientaResponse desde(Herramienta herramienta) {
        return new HerramientaResponse(
                herramienta.getId(),
                herramienta.getProveedorId(),
                herramienta.getNombre(),
                herramienta.getDescripcion(),
                herramienta.getMarca(),
                herramienta.getTarifaDiaria().monto(),
                herramienta.getTarifaDiaria().moneda(),
                herramienta.getEstado(),
                herramienta.getCategoriaIds().stream().sorted().toList(),
                herramienta.getImagenes().stream()
                        .map(imagen -> new ImagenResponse(imagen.getOrden(), imagen.getUrl().valor()))
                        .toList());
    }
}
