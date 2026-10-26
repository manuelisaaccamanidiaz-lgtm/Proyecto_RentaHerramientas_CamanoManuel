package com.rentaherramientas.domain.identity.model.valueobject;

import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;

/**
 * Hash de contraseña ya calculado. El dominio nunca ve la contraseña en claro:
 * el hashing es responsabilidad de infraestructura/seguridad. Tener un tipo propio
 * impide confundirlo con cualquier otro String y evita que se imprima en logs.
 */
public record PasswordHash(String valor) {

    private static final int LONGITUD_MAXIMA = 255;

    /** Constructor compacto: exige un hash no vacío y de longitud compatible con la columna. */
    public PasswordHash {
        if (valor == null || valor.isBlank()) {
            throw new ValorInvalidoException("El hash de contraseña es obligatorio");
        }
        if (valor.length() > LONGITUD_MAXIMA) {
            throw new ValorInvalidoException("El hash de contraseña supera " + LONGITUD_MAXIMA + " caracteres");
        }
    }

    /** Oculta el hash para que no aparezca en logs ni mensajes de error. */
    @Override
    public String toString() {
        return "PasswordHash[****]";
    }
}
