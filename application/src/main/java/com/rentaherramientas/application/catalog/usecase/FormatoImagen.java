package com.rentaherramientas.application.catalog.usecase;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * Formatos de imagen admitidos. Además del tipo MIME declarado por el cliente
 * (que es trivial de falsificar) se comprueba la firma de los primeros bytes,
 * para no publicar como imagen un archivo de otro tipo.
 */
enum FormatoImagen {

    JPEG("image/jpeg") {
        @Override
        boolean firmaValida(byte[] b) {
            return b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
        }
    },
    PNG("image/png") {
        @Override
        boolean firmaValida(byte[] b) {
            byte[] firma = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
            return b.length >= firma.length && Arrays.equals(b, 0, firma.length, firma, 0, firma.length);
        }
    },
    WEBP("image/webp") {
        @Override
        boolean firmaValida(byte[] b) {
            return b.length >= 12
                    && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                    && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
        }
    };

    private final String contentType;

    FormatoImagen(String contentType) {
        this.contentType = contentType;
    }

    abstract boolean firmaValida(byte[] contenido);

    String contentType() {
        return contentType;
    }

    static Optional<FormatoImagen> desdeContentType(String contentType) {
        if (contentType == null) {
            return Optional.empty();
        }
        String normalizado = contentType.split(";")[0].strip().toLowerCase(Locale.ROOT);
        return Arrays.stream(values()).filter(formato -> formato.contentType.equals(normalizado)).findFirst();
    }
}
