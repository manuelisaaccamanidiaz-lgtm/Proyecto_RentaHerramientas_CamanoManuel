package com.rentaherramientas.application.identity.dto;

import com.rentaherramientas.domain.identity.model.valueobject.Rol;

import java.time.Instant;

/**
 * Respuesta de un login exitoso. El tipo siempre es "Bearer": el cliente debe
 * enviar el token en el header Authorization: Bearer &lt;token&gt;.
 */
public record TokenResponse(String token, String tipo, Instant expiraEn, Rol rol) {

    public static final String TIPO_BEARER = "Bearer";

    /**
     * Constructor adicional que fija el tipo de token a "Bearer".
     *
     * TÉCNICA AVANZADA (Java): constructor secundario de record
     *   Qué hace: debe delegar en el constructor canónico con `this(...)`; sirve para ofrecer valores por
     *     defecto.
     */
    public TokenResponse(String token, Instant expiraEn, Rol rol) {
        this(token, TIPO_BEARER, expiraEn, rol);
    }
}
