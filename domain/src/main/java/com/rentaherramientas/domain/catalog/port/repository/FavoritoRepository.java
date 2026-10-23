package com.rentaherramientas.domain.catalog.port.repository;

import com.rentaherramientas.domain.catalog.model.aggregate.Favorito;
import com.rentaherramientas.domain.shared.model.Pagina;
import com.rentaherramientas.domain.shared.model.SolicitudPagina;

/**
 * Puerto de salida de los favoritos. Las dos escrituras son idempotentes: repetir
 * la misma operación deja la base igual que hacerla una sola vez.
 */
public interface FavoritoRepository {

    /** Guarda el favorito; si el cliente ya tenía marcada esa herramienta, no hace nada. */
    void agregar(Favorito favorito);

    /** Borra el favorito; si no existía, no hace nada. */
    void quitar(Long clienteId, Long herramientaId);

    /** Ids de las herramientas favoritas del cliente, de la marcada más reciente a la más antigua. */
    Pagina<Long> herramientaIdsDe(Long clienteId, SolicitudPagina pagina);
}
