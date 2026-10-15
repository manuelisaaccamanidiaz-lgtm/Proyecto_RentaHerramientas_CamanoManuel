package com.rentaherramientas.application.catalog.usecase;

import com.rentaherramientas.application.catalog.command.FiltroHerramientas;
import com.rentaherramientas.application.catalog.dto.HerramientaResponse;
import com.rentaherramientas.application.shared.dto.PaginaResponse;

/**
 * Consultas de herramientas: catálogo público y panel de inventario del proveedor.
 */
public interface ConsultarHerramientaUseCase {

    HerramientaResponse buscarPorId(Long id);

    PaginaResponse<HerramientaResponse> buscar(FiltroHerramientas filtro);
}
