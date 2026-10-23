package com.rentaherramientas.application.catalog.usecase;

import com.rentaherramientas.application.catalog.dto.HerramientaResponse;
import com.rentaherramientas.application.shared.dto.PaginaResponse;

/**
 * Herramientas favoritas del cliente autenticado. Marcar y desmarcar son
 * idempotentes: repetirlas no falla ni cambia el resultado.
 */
public interface GestionarFavoritosUseCase {

    /** Marca la herramienta como favorita del cliente (si ya lo era, no hace nada). */
    void marcar(Long clienteId, Long herramientaId);

    /** Quita la herramienta de los favoritos del cliente (si no estaba, no hace nada). */
    void desmarcar(Long clienteId, Long herramientaId);

    /** Herramientas favoritas del cliente, de la marcada más reciente a la más antigua. */
    PaginaResponse<HerramientaResponse> misFavoritos(Long clienteId, int pagina, int tamano);
}
