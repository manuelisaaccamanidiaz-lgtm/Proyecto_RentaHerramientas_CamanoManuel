package com.rentaherramientas.application.shared.dto;

import com.rentaherramientas.domain.shared.model.Pagina;

import java.util.List;
import java.util.function.Function;

/**
 * Respuesta paginada de los listados. La aplicación no conoce Page de Spring:
 * traduce la Pagina del dominio a este record, que es lo que recibe el cliente.
 */
public record PaginaResponse<T>(List<T> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {

    public static <D, T> PaginaResponse<T> desde(Pagina<D> pagina, Function<? super D, ? extends T> mapper) {
        return new PaginaResponse<>(
                pagina.contenido().stream().<T>map(mapper).toList(),
                pagina.numero(),
                pagina.tamano(),
                pagina.totalElementos(),
                pagina.totalPaginas());
    }
}
