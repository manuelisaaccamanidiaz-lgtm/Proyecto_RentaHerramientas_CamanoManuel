package com.rentaherramientas.infrastructure.shared.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS para el frontend (Vite en http://localhost:5173 por defecto). Spring
 * Security lo aplica vía http.cors(), de modo que los preflight OPTIONS se
 * responden antes de exigir autenticación.
 */
@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig {

    /**
     * Define qué orígenes, métodos y cabeceras puede usar el navegador al llamar a la API desde
     * otro dominio. `setMaxAge(3600)` permite al navegador cachear el preflight una hora.
     *
     * TÉCNICA AVANZADA (Spring): `@EnableConfigurationProperties`
     *   Qué hace: registra el record `CorsProperties` como bean, rellenado desde `application.yml`, y Spring
     *     lo inyecta como parámetro de este método `@Bean`.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource(CorsProperties properties) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(properties.allowedOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
