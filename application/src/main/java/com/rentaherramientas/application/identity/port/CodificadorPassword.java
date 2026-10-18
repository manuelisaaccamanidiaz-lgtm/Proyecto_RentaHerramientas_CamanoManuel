package com.rentaherramientas.application.identity.port;

import com.rentaherramientas.domain.identity.model.valueobject.PasswordHash;

/**
 * Puerto de salida para el hashing de contraseñas. La aplicación decide cuándo
 * codificar o comparar; el algoritmo (BCrypt) lo elige infraestructura.
 */
public interface CodificadorPassword {

    /** Calcula el hash de una contraseña en texto plano. */
    PasswordHash codificar(String plano);

    /** Comprueba si la contraseña en texto plano corresponde al hash guardado. */
    boolean coincide(String plano, PasswordHash hash);
}
