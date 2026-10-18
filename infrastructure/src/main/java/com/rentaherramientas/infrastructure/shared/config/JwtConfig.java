package com.rentaherramientas.infrastructure.shared.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * JWT firmados con HS256 usando el soporte oficial de Spring Security (Nimbus).
 * La misma clave simétrica firma (JwtEncoder) y verifica (JwtDecoder), porque
 * esta API es a la vez emisora y consumidora de sus tokens.
 *
 * TÉCNICA AVANZADA (Spring Security): OAuth2 Resource Server con clave simétrica
 *   Qué hace: basta con exponer un bean `JwtDecoder`; Spring Security lo usa para validar automáticamente el
 *     header `Authorization: Bearer ...` de cada petición.
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    public static final String ISSUER = "renta-herramientas";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_ROL = "rol";

    /** Convierte el secreto de configuración en una clave HMAC-SHA256 para firmar y verificar. */
    @Bean
    SecretKey jwtSecretKey(JwtProperties properties) {
        return new SecretKeySpec(properties.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    /**
     * Codificador que firma los tokens emitidos en el login.
     *
     * Nota: `ImmutableSecret` es la fuente de claves de Nimbus para una clave secreta fija.
     */
    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
    }

    /** Además de firma y expiración, rechaza tokens cuyo "iss" no sea esta API. */
    @Bean
    JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(ISSUER));
        return decoder;
    }

    /** Claim "rol" = "ADMIN" → authority "ROLE_ADMIN", para poder usar hasRole("ADMIN"). */
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName(CLAIM_ROL);
        authorities.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }
}
