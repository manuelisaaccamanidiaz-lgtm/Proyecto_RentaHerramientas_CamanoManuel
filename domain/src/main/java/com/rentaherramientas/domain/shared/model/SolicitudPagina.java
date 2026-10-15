package com.rentaherramientas.domain.shared.model;

import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;

/**
 * Página pedida por un listado: número (desde 0) y tamaño. El tope de tamaño
 * protege a la base de datos de consultas que devuelvan miles de filas de golpe.
 */
public record SolicitudPagina(int numero, int tamano) {

    public static final int TAMANO_MAXIMO = 50;
    public static final int TAMANO_POR_DEFECTO = 20;

    public SolicitudPagina {
        if (numero < 0) {
            throw new ValorInvalidoException("El número de página no puede ser negativo");
        }
        if (tamano < 1 || tamano > TAMANO_MAXIMO) {
            throw new ValorInvalidoException("El tamaño de página debe estar entre 1 y " + TAMANO_MAXIMO);
        }
    }
}
