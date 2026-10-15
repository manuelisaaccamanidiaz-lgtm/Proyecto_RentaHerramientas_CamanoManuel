package com.rentaherramientas.domain.shared.model;

import java.util.List;

/**
 * Resultado paginado que devuelven los puertos de repositorio. Es propio del
 * dominio para que ni el dominio ni la aplicación dependan de Page de Spring Data.
 */
public record Pagina<T>(List<T> contenido, int numero, int tamano, long totalElementos) {

    public Pagina {
        contenido = List.copyOf(contenido);
    }

    public int totalPaginas() {
        return tamano == 0 ? 0 : (int) Math.ceil((double) totalElementos / tamano);
    }
}
