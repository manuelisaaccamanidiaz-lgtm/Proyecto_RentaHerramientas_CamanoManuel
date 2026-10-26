package com.rentaherramientas.infrastructure.identity.adapters.out.security;

import com.rentaherramientas.application.identity.port.GeneradorToken;
import com.rentaherramientas.application.identity.port.TokenEmitido;
import com.rentaherramientas.domain.identity.model.aggregate.Usuario;
import com.rentaherramientas.infrastructure.shared.config.JwtConfig;
import com.rentaherramientas.infrastructure.shared.config.JwtProperties;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Adaptador de salida: emite un JWT HS256 con sub = id del usuario, email, rol,
 * iss, iat y exp. El claim "rol" es el que JwtConfig convierte en ROLE_*.
 */
@Component
public class JwtGeneradorToken implements GeneradorToken {

    private final JwtEncoder jwtEncoder;
    private final Duration expiracion;

    /** Recibe el codificador JWT y calcula la duración de los tokens a partir de la configuración. */
    public JwtGeneradorToken(JwtEncoder jwtEncoder, JwtProperties properties) {
        this.jwtEncoder = jwtEncoder;
        this.expiracion = Duration.ofMinutes(properties.expiracionMinutos());
    }

    /**
     * Construye los claims del usuario, los firma con HS256 y devuelve el token y su expiración.
     *
     * TÉCNICA AVANZADA (Spring Security): `JwtClaimsSet.builder()`
     *   Qué hace: patrón Builder para armar el contenido del token. `sub` lleva el id; `iss`, `iat` y `exp`
     *     son claims estándar que JwtDecoder valida al recibir el token.
     */
    @Override
    public TokenEmitido generar(Usuario usuario) {
        Instant ahora = Instant.now();
        Instant expiraEn = ahora.plus(expiracion);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(JwtConfig.ISSUER)
                .subject(String.valueOf(usuario.getId()))
                .issuedAt(ahora)
                .expiresAt(expiraEn)
                .claim(JwtConfig.CLAIM_EMAIL, usuario.getEmail().valor())
                .claim(JwtConfig.CLAIM_ROL, usuario.getRol().name())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenEmitido(token, expiraEn);
    }
}
