package com.rentaherramientas.application.catalog.usecase;

import com.rentaherramientas.application.catalog.command.FiltroHerramientas;
import com.rentaherramientas.application.catalog.dto.HerramientaResponse;
import com.rentaherramientas.application.shared.dto.PaginaResponse;

/**
 * Consultas de herramientas: catálogo público y panel de inventario del proveedor.
 */
public interface ConsultarHerramientaUseCase {

    /** Devuelve la herramienta con ese id. */
    HerramientaResponse buscarPorId(Long id);

    /** Busca herramientas con filtros opcionales y paginación. */
    PaginaResponse<HerramientaResponse> buscar(FiltroHerramientas filtro);
}
