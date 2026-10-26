package com.rentaherramientas.application.catalog.usecase;

import com.rentaherramientas.application.catalog.command.AgregarImagenCommand;
import com.rentaherramientas.application.catalog.dto.HerramientaResponse;
import com.rentaherramientas.application.shared.command.Solicitante;

/**
 * Galería de imágenes de una herramienta (su proveedor o un ADMIN).
 */
public interface GestionarImagenesHerramientaUseCase {

    /** Sube una imagen nueva a la herramienta. */
    HerramientaResponse agregar(AgregarImagenCommand command);

    /** Quita la imagen que ocupa la posición indicada. */
    HerramientaResponse quitar(Long herramientaId, int orden, Solicitante solicitante);
}
