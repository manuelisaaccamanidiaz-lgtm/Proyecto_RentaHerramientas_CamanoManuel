package com.rentaherramientas.infrastructure.shared.adapters.in.rest;

import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.domain.identity.model.valueobject.Rol;
import com.rentaherramientas.infrastructure.shared.config.JwtConfig;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Construye el Solicitante de un command a partir del JWT ya validado por Spring
 * Security: id = claim "sub", rol = claim "rol". Los controllers nunca toman la
 * identidad del cuerpo de la petición.
 */
public final class SolicitanteJwt {

    private SolicitanteJwt() {
    }

    public static Solicitante desde(Jwt jwt) {
        return new Solicitante(Long.valueOf(jwt.getSubject()), Rol.valueOf(jwt.getClaimAsString(JwtConfig.CLAIM_ROL)));
    }
}
