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
    private static final String CLIENTE = "CLIENTE";

    /**
     * Define la cadena de filtros de seguridad: sin sesiones, CORS, reglas por URL y rol, y JWT.
     *
     * TÉCNICA AVANZADA (Spring Security 6): DSL con lambdas
     *   Qué hace: cada sección (`csrf`, `cors`, `authorizeHttpRequests`...) se configura con una lambda. El
     *     orden de los `requestMatchers` importa: gana la primera regla que coincide, por eso las reglas
     *     concretas van antes que las generales y `anyRequest()` va al final.
     *
     * Nota: `hasRole("ADMIN")` comprueba la authority `ROLE_ADMIN`, que crea el `JwtAuthenticationConverter`
     *   de JwtConfig a partir del claim "rol".
     *
     * Nota: `SessionCreationPolicy.STATELESS`: el servidor no guarda sesión; cada petición se autentica solo
     *   con su token.
     */
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
                        // Healthcheck de Docker / balanceadores (solo expone UP/DOWN, sin detalles).
                        .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**").permitAll()
                        // Frontend (módulo frontend, classpath:/static/): las páginas, estilos y scripts
                        // son públicos; los DATOS que muestran los protege la API con el JWT. /img
                        // incluye las ilustraciones de los datos semilla.
                        .requestMatchers(HttpMethod.GET, "/", "/*.html", "/css/**", "/js/**", "/img/**",
                                "/cliente/**", "/proveedor/**", "/admin/**").permitAll()
                        // Imágenes subidas por los proveedores, servidas como recursos estáticos.
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        // Catálogo público de solo lectura. La disponibilidad (rental) va ANTES de la
                        // regla general /api/herramientas/**, que la dejaría solo para PROVEEDOR/ADMIN.
                        .requestMatchers(HttpMethod.GET, "/api/categorias", "/api/categorias/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/herramientas", "/api/herramientas/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/herramientas/*/disponibilidad").permitAll()
                        // Administración de categorías.
                        .requestMatchers("/api/categorias/**").hasRole(ADMIN)
                        // Herramientas: alta solo PROVEEDOR; el resto, PROVEEDOR (dueño) o ADMIN.
                        .requestMatchers(HttpMethod.POST, "/api/herramientas").hasRole(PROVEEDOR)
                        .requestMatchers("/api/herramientas/**").hasAnyRole(PROVEEDOR, ADMIN)
                        // Reservas: el cliente las crea y consulta las suyas; el proveedor (dueño) o un
                        // ADMIN las gestionan. GET /api/reservas/{id} queda para cualquier autenticado:
                        // quién puede verla lo decide el caso de uso.
                        .requestMatchers(HttpMethod.POST, "/api/reservas").hasRole(CLIENTE)
                        .requestMatchers(HttpMethod.GET, "/api/reservas/mias").hasRole(CLIENTE)
                        .requestMatchers(HttpMethod.PATCH, "/api/reservas/*/cancelar").hasRole(CLIENTE)
                        .requestMatchers(HttpMethod.PATCH, "/api/reservas/*/*").hasAnyRole(PROVEEDOR, ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/reservas/*/danios").hasAnyRole(PROVEEDOR, ADMIN)
                        // Pagos y facturas: el cliente paga y consulta los suyos; el proveedor (dueño) o
                        // un ADMIN confirman/rechazan pagos en efectivo. GET /api/pagos/{id},
                        // /api/facturas/{id} y su PDF quedan para cualquier autenticado: el acceso
                        // (cliente, proveedor o ADMIN de esa reserva) lo decide el caso de uso.
                        .requestMatchers(HttpMethod.POST, "/api/pagos").hasRole(CLIENTE)
                        .requestMatchers(HttpMethod.GET, "/api/pagos/mios", "/api/facturas/mias").hasRole(CLIENTE)
                        .requestMatchers(HttpMethod.PATCH, "/api/pagos/*/*").hasAnyRole(PROVEEDOR, ADMIN)
                        // Favoritos: marcar, desmarcar y listar son solo del cliente.
                        .requestMatchers("/api/favoritos/**").hasRole(CLIENTE)
                        .requestMatchers("/api/proveedor/**").hasRole(PROVEEDOR)
                        .requestMatchers("/api/admin/**").hasRole(ADMIN)
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
        return http.build();
    }

    /**
     * Codificador de contraseñas BCrypt.
     *
     * Técnica avanzada — BCrypt: algoritmo de hash lento a propósito y con "sal" aleatoria: dos contraseñas
     *   iguales dan hashes distintos y los ataques de fuerza bruta son costosos. Se verifica con
     *   `matches(plano, hash)`, nunca comparando hashes.
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
