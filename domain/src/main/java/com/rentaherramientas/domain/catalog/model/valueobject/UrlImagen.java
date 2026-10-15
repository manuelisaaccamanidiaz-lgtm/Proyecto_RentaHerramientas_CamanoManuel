package com.rentaherramientas.domain.catalog.model.valueobject;

import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * URL absoluta http/https de una imagen. Validarla en el dominio evita guardar
 * rutas locales o esquemas peligrosos (javascript:, file:) que luego se mostrarían
 * en el frontend.
 */
public record UrlImagen(String valor) {

    private static final int LONGITUD_MAXIMA = 500;

    public UrlImagen {
        if (valor == null || valor.isBlank()) {
            throw new ValorInvalidoException("La URL de la imagen es obligatoria");
        }
        valor = valor.strip();
        if (valor.length() > LONGITUD_MAXIMA) {
            throw new ValorInvalidoException("La URL supera " + LONGITUD_MAXIMA + " caracteres");
        }
        try {
            URI uri = new URI(valor);
            String esquema = uri.getScheme();
            if (esquema == null || !(esquema.equalsIgnoreCase("http") || esquema.equalsIgnoreCase("https"))
                    || uri.getHost() == null) {
                throw new ValorInvalidoException("La URL debe ser absoluta http/https: " + valor);
            }
        } catch (URISyntaxException e) {
            throw new ValorInvalidoException("La URL no es válida: " + valor);
        }
    }
}
