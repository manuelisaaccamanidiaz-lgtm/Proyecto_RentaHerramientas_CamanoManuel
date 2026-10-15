package com.rentaherramientas.infrastructure.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Seguridad de la API: stateless, autenticación con JWT Bearer (resource server)
 * y autorización por rol. CSRF se deshabilita porque la API no usa cookies de
 * sesión: el token viaja en el header Authorization, que el navegador no envía
 * automáticamente en peticiones cruzadas. Aquí solo se filtra por ROL; la regla
 * de propiedad (dueño de la herramienta) la aplican los casos de uso.
 */
@Configuration
public class SecurityConfig {

    private static final String PROVEEDOR = "PROVEEDOR";
    private static final String ADMIN = "ADMIN";

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        // Spring Boot reenvía los errores a /error; sin esto un 400/404 llegaría como 401.
                        .requestMatchers("/error").permitAll()
                        // Imágenes del catálogo, servidas como recursos estáticos.
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        // Catálogo público de solo lectura.
                        .requestMatchers(HttpMethod.GET, "/api/categorias", "/api/categorias/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/herramientas", "/api/herramientas/*").permitAll()
                        // Administración de categorías.
                        .requestMatchers("/api/categorias/**").hasRole(ADMIN)
                        // Herramientas: alta solo PROVEEDOR; el resto, PROVEEDOR (dueño) o ADMIN.
                        .requestMatchers(HttpMethod.POST, "/api/herramientas").hasRole(PROVEEDOR)
                        .requestMatchers("/api/herramientas/**").hasAnyRole(PROVEEDOR, ADMIN)
                        .requestMatchers("/api/proveedor/**").hasRole(PROVEEDOR)
                        .requestMatchers("/api/admin/**").hasRole(ADMIN)
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
