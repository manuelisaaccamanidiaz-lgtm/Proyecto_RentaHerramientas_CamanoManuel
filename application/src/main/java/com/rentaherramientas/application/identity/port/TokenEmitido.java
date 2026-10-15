package com.rentaherramientas.application.identity.port;

import java.time.Instant;

/**
 * Resultado de emitir un token: el valor opaco y el instante en que expira.
 */
public record TokenEmitido(String token, Instant expiraEn) {
}
