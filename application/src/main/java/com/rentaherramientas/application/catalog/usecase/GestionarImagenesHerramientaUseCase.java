package com.rentaherramientas.application.catalog.usecase;

import com.rentaherramientas.application.catalog.command.AgregarImagenCommand;
import com.rentaherramientas.application.catalog.dto.HerramientaResponse;
import com.rentaherramientas.application.shared.command.Solicitante;

/**
 * Galería de imágenes de una herramienta (su proveedor o un ADMIN).
 */
public interface GestionarImagenesHerramientaUseCase {

    HerramientaResponse agregar(AgregarImagenCommand command);

    HerramientaResponse quitar(Long herramientaId, int orden, Solicitante solicitante);
}
