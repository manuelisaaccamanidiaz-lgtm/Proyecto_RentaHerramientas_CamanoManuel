package com.rentaherramientas.domain.catalog.model.valueobject;

import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

/**
 * Ubicación de una imagen. Admite dos formas:
 * - URL absoluta http/https (imágenes alojadas fuera de la API);
 * - ruta relativa a la raíz de la API que empieza con "/" (p. ej.
 *   /uploads/abc.png o /img/seed/taladro.svg). Así los datos no dependen del
 *   dominio en que se despliegue la API: el frontend antepone su URL base.
 * Validarla en el dominio evita guardar rutas locales o esquemas peligrosos
 * (javascript:, file:) que luego se mostrarían en el frontend.
 */
public record UrlImagen(String valor) {

    private static final int LONGITUD_MAXIMA = 500;
    /** Segmentos de ruta con caracteres seguros; nada de espacios, "\", "?" ni "#". */
    private static final Pattern RUTA_RELATIVA = Pattern.compile("^(/[A-Za-z0-9._~-]+)+$");

    public UrlImagen {
        if (valor == null || valor.isBlank()) {
            throw new ValorInvalidoException("La URL de la imagen es obligatoria");
        }
        valor = valor.strip();
        if (valor.length() > LONGITUD_MAXIMA) {
            throw new ValorInvalidoException("La URL supera " + LONGITUD_MAXIMA + " caracteres");
        }
        if (valor.startsWith("/")) {
            validarRutaRelativa(valor);
        } else {
            validarUrlAbsoluta(valor);
        }
    }

    public boolean esRelativa() {
        return valor.startsWith("/");
    }

    /**
     * "//host/x" es una URL relativa al protocolo que apunta a OTRO servidor, y
     * ".." permitiría salir del directorio público: ambas se rechazan.
     */
    private static void validarRutaRelativa(String ruta) {
        if (ruta.startsWith("//") || !RUTA_RELATIVA.matcher(ruta).matches()) {
            throw new ValorInvalidoException("La ruta de la imagen no es válida: " + ruta);
        }
        for (String segmento : ruta.substring(1).split("/")) {
            if (segmento.equals(".") || segmento.equals("..")) {
                throw new ValorInvalidoException("La ruta de la imagen no puede contener '.' ni '..': " + ruta);
            }
        }
    }

    private static void validarUrlAbsoluta(String url) {
        try {
            URI uri = new URI(url);
            String esquema = uri.getScheme();
            if (esquema == null || !(esquema.equalsIgnoreCase("http") || esquema.equalsIgnoreCase("https"))
                    || uri.getHost() == null) {
                throw new ValorInvalidoException(
                        "La URL debe ser absoluta http/https o una ruta que empiece con '/': " + url);
            }
        } catch (URISyntaxException e) {
            throw new ValorInvalidoException("La URL no es válida: " + url);
        }
    }
}
