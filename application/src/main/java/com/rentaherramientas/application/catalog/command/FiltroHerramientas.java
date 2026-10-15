package com.rentaherramientas.application.catalog.command;

import com.rentaherramientas.domain.catalog.model.valueobject.EstadoHerramienta;

/**
 * Filtros opcionales (null = sin filtrar) del listado de herramientas.
 * soloCatalogoPublico oculta las herramientas INACTIVAS (dadas de baja) cuando
 * no se pide un estado concreto; el panel del proveedor las muestra todas.
 */
public record FiltroHerramientas(Long categoriaId, EstadoHerramienta estado, String texto, Long proveedorId,
                                 boolean soloCatalogoPublico, int pagina, int tamano) {
}
