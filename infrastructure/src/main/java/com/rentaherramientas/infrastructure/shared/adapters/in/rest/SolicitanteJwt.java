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

    /** Constructor privado: clase de utilidades estáticas. */
    private SolicitanteJwt() {
    }

    /**
     * Extrae el id (claim "sub") y el rol (claim "rol") del token validado.
     *
     * Cómo usarlo (Spring Security): en un controller, declara el parámetro
     *   `@AuthenticationPrincipal Jwt jwt` y Spring inyecta el token ya verificado de la petición; luego
     *   llama a `SolicitanteJwt.desde(jwt)`.
     */
    public static Solicitante desde(Jwt jwt) {
        return new Solicitante(Long.valueOf(jwt.getSubject()), Rol.valueOf(jwt.getClaimAsString(JwtConfig.CLAIM_ROL)));
    }
}
