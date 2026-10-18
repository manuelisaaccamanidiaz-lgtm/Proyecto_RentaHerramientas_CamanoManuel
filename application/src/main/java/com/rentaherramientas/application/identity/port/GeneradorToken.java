package com.rentaherramientas.application.identity.port;

import com.rentaherramientas.domain.identity.model.aggregate.Usuario;

/**
 * Puerto de salida para emitir el token de acceso de un usuario autenticado.
 * La aplicación no sabe que es un JWT ni cómo se firma.
 */
public interface GeneradorToken {

    /** Genera un token de acceso firmado para el usuario autenticado. */
    TokenEmitido generar(Usuario usuario);
}
