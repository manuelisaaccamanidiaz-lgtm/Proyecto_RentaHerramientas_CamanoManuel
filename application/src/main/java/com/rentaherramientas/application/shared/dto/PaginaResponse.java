package com.rentaherramientas.application.shared.dto;

import com.rentaherramientas.domain.shared.model.Pagina;

import java.util.List;
import java.util.function.Function;

/**
 * Respuesta paginada de los listados. La aplicación no conoce Page de Spring:
 * traduce la Pagina del dominio a este record, que es lo que recibe el cliente.
 */
public record PaginaResponse<T>(List<T> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {

    /**
     * Convierte una Pagina del dominio en la respuesta de la API, transformando cada elemento con `mapper`.
     *
     * TÉCNICA AVANZADA (Java): genéricos con comodines (PECS)
     *   Qué hace: `Function<? super D, ? extends T>` acepta funciones que reciben un supertipo de D y
     *     devuelven un subtipo de T ("Producer Extends, Consumer Super"), lo que hace el método más flexible.
     *     `.<T>map(mapper)` fija explícitamente el tipo del stream resultante.
     *   Cómo usarlo: `PaginaResponse.desde(pagina, UsuarioResponse::desde)`.
     */
    public static <D, T> PaginaResponse<T> desde(Pagina<D> pagina, Function<? super D, ? extends T> mapper) {
        return new PaginaResponse<>(
                pagina.contenido().stream().<T>map(mapper).toList(),
                pagina.numero(),
                pagina.tamano(),
                pagina.totalElementos(),
                pagina.totalPaginas());
    }
}
