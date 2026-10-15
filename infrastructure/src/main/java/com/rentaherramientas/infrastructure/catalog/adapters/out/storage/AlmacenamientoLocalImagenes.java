package com.rentaherramientas.infrastructure.catalog.adapters.out.storage;

import com.rentaherramientas.application.catalog.port.AlmacenamientoImagenes;
import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Adaptador de salida: guarda las imágenes en un directorio local con nombre
 * UUID + extensión (el nombre original del cliente nunca se usa, así no hay
 * colisiones ni rutas maliciosas) y devuelve la URL absoluta con la que
 * UploadsWebConfig las sirve en /uploads/**.
 */
@Component
public class AlmacenamientoLocalImagenes implements AlmacenamientoImagenes {

    public static final String RUTA_PUBLICA = "/uploads/";

    private static final Logger log = LoggerFactory.getLogger(AlmacenamientoLocalImagenes.class);
    /** Solo nombres generados por este adaptador: impide borrar fuera del directorio (../). */
    private static final Pattern NOMBRE_VALIDO = Pattern.compile("^[0-9a-f-]{36}\\.(jpg|png|webp)$");

    private final Path directorio;
    private final String prefijoUrl;

    public AlmacenamientoLocalImagenes(@Value("${app.uploads.dir:./uploads}") String directorio,
                                       @Value("${app.public-base-url:http://localhost:8080}") String baseUrl) {
        this.directorio = Path.of(directorio).toAbsolutePath().normalize();
        this.prefijoUrl = baseUrl.replaceAll("/+$", "") + RUTA_PUBLICA;
        try {
            Files.createDirectories(this.directorio);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo crear el directorio de imágenes " + this.directorio, e);
        }
        log.info("Imágenes en {} publicadas como {}*", this.directorio, prefijoUrl);
    }

    @Override
    public String guardar(byte[] contenido, String contentType) {
        String nombre = UUID.randomUUID() + extension(contentType);
        try {
            Files.write(directorio.resolve(nombre), contenido, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar la imagen " + nombre, e);
        }
        return prefijoUrl + nombre;
    }

    @Override
    public void eliminar(String url) {
        if (url == null || !url.startsWith(prefijoUrl)) {
            return;
        }
        String nombre = url.substring(prefijoUrl.length());
        if (!NOMBRE_VALIDO.matcher(nombre).matches()) {
            return;
        }
        try {
            Files.deleteIfExists(directorio.resolve(nombre));
        } catch (IOException e) {
            // El registro en BD ya no la referencia: un archivo huérfano no debe romper la operación.
            log.warn("No se pudo borrar la imagen {}", nombre, e);
        }
    }

    private static String extension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new ValorInvalidoException("Formato de imagen no admitido: " + contentType);
        };
    }
}
