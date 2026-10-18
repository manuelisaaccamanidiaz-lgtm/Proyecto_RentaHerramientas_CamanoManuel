package com.rentaherramientas.infrastructure.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Propiedades app.cors.*: orígenes del frontend autorizados a llamar a la API.
 *
 * TÉCNICA AVANZADA (Spring Boot): `@ConfigurationProperties` con record
 *   Qué hace: Spring asigna `app.cors.allowed-origins` (kebab-case en YAML) al componente `allowedOrigins`
 *     (camelCase) usando el constructor. Es configuración tipada e inmutable.
 *   Cómo usarlo: declara un record con `@ConfigurationProperties("prefijo")` y actívalo con
 *     `@EnableConfigurationProperties(MiRecord.class)`.
 */
@ConfigurationProperties("app.cors")
public record CorsProperties(List<String> allowedOrigins) {

    /** Constructor compacto: si no se configura ningún origen, usa http://localhost:5173. */
    public CorsProperties {
        allowedOrigins = allowedOrigins == null || allowedOrigins.isEmpty()
                ? List.of("http://localhost:5173")
                : List.copyOf(allowedOrigins);
    }
}
