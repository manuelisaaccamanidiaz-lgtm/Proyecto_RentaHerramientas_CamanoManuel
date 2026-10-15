package com.rentaherramientas.domain.catalog.port.repository;

import com.rentaherramientas.domain.catalog.model.valueobject.EstadoHerramienta;
import com.rentaherramientas.domain.shared.model.SolicitudPagina;
import com.rentaherramientas.domain.shared.model.Validaciones;

/**
 * Filtros opcionales (null = sin filtrar) para buscar herramientas. El texto se
 * busca en nombre y descripción sin distinguir mayúsculas. Si excluirInactivas es
 * true y no se pide un estado concreto, se ocultan las herramientas dadas de baja.
 */
public record CriteriosBusquedaHerramienta(Long categoriaId,
                                           EstadoHerramienta estado,
                                           String texto,
                                           Long proveedorId,
                                           boolean excluirInactivas,
                                           SolicitudPagina pagina) {

    public CriteriosBusquedaHerramienta {
        texto = texto == null || texto.isBlank() ? null : texto.strip();
        Validaciones.requerido(pagina, "pagina");
    }
}
