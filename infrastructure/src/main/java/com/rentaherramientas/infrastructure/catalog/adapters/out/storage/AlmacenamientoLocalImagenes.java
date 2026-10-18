package com.rentaherramientas.infrastructure.catalog.adapters.out.storage;

import com.rentaherramientas.application.catalog.port.AlmacenamientoImagenes;
import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Adaptador de salida: guarda las imágenes en un directorio local con nombre
 * UUID + extensión (el nombre original del cliente nunca se usa, así no hay
 * colisiones ni rutas maliciosas) y devuelve la ruta RELATIVA /uploads/<archivo>,
 * que UploadsWebConfig sirve. Al no incluir dominio, los datos siguen siendo
 * válidos si la API cambia de URL; el frontend antepone su URL base.
 */
@Component
public class AlmacenamientoLocalImagenes implements AlmacenamientoImagenes {

    public static final String RUTA_PUBLICA = "/uploads/";

    private static final Logger log = LoggerFactory.getLogger(AlmacenamientoLocalImagenes.class);
    /** Solo nombres generados por este adaptador: impide borrar fuera del directorio (../). */
    private static final Pattern NOMBRE_VALIDO = Pattern.compile("^[0-9a-f-]{36}\\.(jpg|png|webp)$");

    private final Path directorio;

    /**
     * Crea el directorio de imágenes si no existe.
     *
     * TÉCNICA AVANZADA (Spring): `@Value("${clave:valorPorDefecto}")`
     *   Qué hace: inyecta una propiedad de configuración; lo que va después de los dos puntos se usa si la
     *     propiedad no está definida. `toAbsolutePath().normalize()` resuelve rutas como "./" y "../".
     */
    public AlmacenamientoLocalImagenes(@Value("${app.uploads.dir:./uploads}") String directorio) {
        this.directorio = Path.of(directorio).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.directorio);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo crear el directorio de imágenes " + this.directorio, e);
        }
        log.info("Imágenes en {} publicadas como {}*", this.directorio, RUTA_PUBLICA);
    }

    /**
     * Escribe el archivo con un nombre UUID y devuelve su ruta pública relativa.
     *
     * Nota: `StandardOpenOption.CREATE_NEW` falla si el archivo ya existe, así nunca se sobrescribe una
     *   imagen. `UncheckedIOException` envuelve la `IOException` comprobada para no obligar a declararla en
     *   la interfaz.
     */
    @Override
    public String guardar(byte[] contenido, String contentType) {
        String nombre = UUID.randomUUID() + extension(contentType);
        try {
            Files.write(directorio.resolve(nombre), contenido, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar la imagen " + nombre, e);
        }
        return RUTA_PUBLICA + nombre;
    }

    /**
     * Acepta la ruta relativa y, por compatibilidad, URLs absolutas antiguas (http://host/uploads/...).
     *
     * TÉCNICA AVANZADA (seguridad): prevención de path traversal
     *   Qué hace: solo se borran nombres que cumplen el patrón de UUID, así una URL manipulada como
     *     "/uploads/../config" no puede borrar archivos fuera del directorio.
     */
    @Override
    public void eliminar(String url) {
        String ruta = rutaDe(url);
        if (ruta == null || !ruta.startsWith(RUTA_PUBLICA)) {
            return;
        }
        String nombre = ruta.substring(RUTA_PUBLICA.length());
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

    /** Devuelve la parte de ruta de la URL (o la URL si ya es relativa); null si no es válida. */
    private static String rutaDe(String url) {
        if (url == null) {
            return null;
        }
        if (url.startsWith("/")) {
            return url;
        }
        try {
            return URI.create(url).getPath();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Devuelve la extensión de archivo correspondiente al tipo MIME. */
    private static String extension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new ValorInvalidoException("Formato de imagen no admitido: " + contentType);
        };
    }
}
