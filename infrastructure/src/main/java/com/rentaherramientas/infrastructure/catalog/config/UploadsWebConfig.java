package com.rentaherramientas.infrastructure.catalog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

/**
 * Sirve como recursos estáticos las imágenes que guarda AlmacenamientoLocalImagenes.
 * SecurityConfig deja /uploads/** público porque el catálogo es público.
 */
@Configuration
public class UploadsWebConfig implements WebMvcConfigurer {

    private final String ubicacion;

    public UploadsWebConfig(@Value("${app.uploads.dir:./uploads}") String directorio) {
        // La barra final es obligatoria: sin ella Spring trata la ubicación como un archivo.
        this.ubicacion = Path.of(directorio).toAbsolutePath().normalize().toUri().toString().replaceAll("/?$", "/");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**").addResourceLocations(ubicacion);
    }
}
