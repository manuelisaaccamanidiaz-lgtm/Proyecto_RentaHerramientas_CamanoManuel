package com.rentaherramientas.domain.shared.model;

import java.util.List;

/**
 * Resultado paginado que devuelven los puertos de repositorio. Es propio del
 * dominio para que ni el dominio ni la aplicación dependan de Page de Spring Data.
 *
 * TÉCNICA AVANZADA (Java): record genérico
 *   Qué hace: `record Pagina<T>` genera constructor, getters, `equals`, `hashCode` y `toString`, y el
 *     parámetro de tipo `T` permite reutilizarlo para cualquier elemento (`Pagina<Usuario>`,
 *     `Pagina<Reserva>`...).
 *   Cómo usarlo: `new Pagina<>(lista, 0, 20, total)`.
 */
public record Pagina<T>(List<T> contenido, int numero, int tamano, long totalElementos) {

    /**
     * TÉCNICA AVANZADA (Java): constructor compacto de record
     *   Qué hace: se ejecuta antes de asignar los campos y permite validar o normalizar los parámetros. Aquí
     *     `List.copyOf` hace una copia defensiva inmutable, de modo que nadie pueda modificar la página desde
     *     fuera.
     *   Cómo usarlo: escribe `public NombreRecord { ... }` sin paréntesis y reasigna los parámetros que
     *     quieras normalizar.
     */
    public Pagina {
        contenido = List.copyOf(contenido);
    }

    /** Calcula cuántas páginas hay redondeando hacia arriba; devuelve 0 si el tamaño es 0. */
    public int totalPaginas() {
        return tamano == 0 ? 0 : (int) Math.ceil((double) totalElementos / tamano);
    }
}
