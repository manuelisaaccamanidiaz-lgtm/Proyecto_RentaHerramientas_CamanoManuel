package com.rentaherramientas.application.identity.port;

import com.rentaherramientas.domain.identity.model.aggregate.Usuario;

/**
 * Puerto de salida para emitir el token de acceso de un usuario autenticado.
 * La aplicación no sabe que es un JWT ni cómo se firma.
 */
public interface GeneradorToken {

    TokenEmitido generar(Usuario usuario);
}
