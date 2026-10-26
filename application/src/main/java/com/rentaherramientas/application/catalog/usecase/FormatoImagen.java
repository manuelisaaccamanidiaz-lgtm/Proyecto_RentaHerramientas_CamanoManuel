package com.rentaherramientas.application.catalog.usecase;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * Formatos de imagen admitidos. Además del tipo MIME declarado por el cliente
 * (que es trivial de falsificar) se comprueba la firma de los primeros bytes,
 * para no publicar como imagen un archivo de otro tipo.
 *
 * TÉCNICA AVANZADA (Java): enum con cuerpo por constante
 *   Qué hace: cada constante (`JPEG`, `PNG`, `WEBP`) implementa su propia versión del método abstracto
 *     `firmaValida(byte[])`. Es el patrón Strategy sin clases extra.
 *
 * Técnica avanzada — "magic numbers" de archivo: los primeros bytes de un archivo identifican su formato
 *   real. Comprobarlos impide subir, por ejemplo, un ejecutable renombrado como .png, aunque el navegador
 *   diga que es `image/png`.
 */
enum FormatoImagen {

    /**
     * JPEG: empieza por los bytes FF D8 FF.
     *
     * Nota: `(b[0] & 0xFF)` convierte el byte con signo de Java (-128..127) a un entero 0..255 para poder
     *   compararlo con valores hexadecimales como `0xFF`.
     */
    JPEG("image/jpeg") {
        @Override
        boolean firmaValida(byte[] b) {
            return b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
        }
    },
    /** PNG: empieza por la firma de 8 bytes 89 50 4E 47 0D 0A 1A 0A. */
    PNG("image/png") {
        @Override
        boolean firmaValida(byte[] b) {
            byte[] firma = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
            return b.length >= firma.length && Arrays.equals(b, 0, firma.length, firma, 0, firma.length);
        }
    },
    /** WEBP: contenedor RIFF con "WEBP" en los bytes 8 a 11. */
    WEBP("image/webp") {
        @Override
        boolean firmaValida(byte[] b) {
            return b.length >= 12
                    && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                    && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
        }
    };

    private final String contentType;

    /** Constructor del enum: asocia a cada formato su tipo MIME. */
    FormatoImagen(String contentType) {
        this.contentType = contentType;
    }

    /** Indica si los primeros bytes del contenido corresponden a este formato. */
    abstract boolean firmaValida(byte[] contenido);

    /** Tipo MIME del formato, por ejemplo "image/png". */
    String contentType() {
        return contentType;
    }

    /** Busca el formato por su tipo MIME, ignorando parámetros como "; charset=" y mayúsculas. */
    static Optional<FormatoImagen> desdeContentType(String contentType) {
        if (contentType == null) {
            return Optional.empty();
        }
        String normalizado = contentType.split(";")[0].strip().toLowerCase(Locale.ROOT);
        return Arrays.stream(values()).filter(formato -> formato.contentType.equals(normalizado)).findFirst();
    }
}
