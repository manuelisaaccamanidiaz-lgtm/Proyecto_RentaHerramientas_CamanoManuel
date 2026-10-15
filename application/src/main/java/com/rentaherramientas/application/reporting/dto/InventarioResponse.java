package com.rentaherramientas.application.reporting.dto;

import java.util.List;
import java.util.Map;

/**
 * Inventario: herramientas por estado y por categoría. Una herramienta con
 * varias categorías cuenta en cada una de ellas.
 */
public record InventarioResponse(long totalHerramientas, Map<String, Long> porEstado,
                                 List<ConteoCategoria> porCategoria) {

    public record ConteoCategoria(Long categoriaId, String categoria, long herramientas) {
    }
}
