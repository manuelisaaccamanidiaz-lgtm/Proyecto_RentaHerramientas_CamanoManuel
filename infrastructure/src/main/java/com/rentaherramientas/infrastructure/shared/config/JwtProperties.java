package com.rentaherramientas.infrastructure.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;

/**
 * Propiedades app.jwt.*. HS256 exige una clave de al menos 256 bits, por eso se
 * valida al arrancar: un secreto corto hace fallar la app en lugar de firmar
 * tokens débiles.
 */
@ConfigurationProperties("app.jwt")
public record JwtProperties(String secret, long expiracionMinutos) {

    private static final int SECRETO_MINIMO_BYTES = 32;

    public JwtProperties {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < SECRETO_MINIMO_BYTES) {
            throw new IllegalStateException(
                    "app.jwt.secret (JWT_SECRET) debe tener al menos " + SECRETO_MINIMO_BYTES + " bytes");
        }
        if (expiracionMinutos <= 0) {
            throw new IllegalStateException("app.jwt.expiracion-minutos debe ser mayor que 0");
        }
    }

    @Override
    public String toString() {
        return "JwtProperties[secret=****, expiracionMinutos=" + expiracionMinutos + "]";
    }
}
