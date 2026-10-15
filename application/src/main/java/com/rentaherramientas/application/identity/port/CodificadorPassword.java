package com.rentaherramientas.application.identity.port;

import com.rentaherramientas.domain.identity.model.valueobject.PasswordHash;

/**
 * Puerto de salida para el hashing de contraseñas. La aplicación decide cuándo
 * codificar o comparar; el algoritmo (BCrypt) lo elige infraestructura.
 */
public interface CodificadorPassword {

    PasswordHash codificar(String plano);

    boolean coincide(String plano, PasswordHash hash);
}
