package com.rentaherramientas.domain.shared.model;

import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;

/**
 * Utilidades de validación reutilizadas por agregados y value objects para no
 * repetir las mismas comprobaciones (nulos, textos vacíos, longitudes) y para
 * que todas lancen la misma excepción de dominio en lugar de NPE genéricas.
 */
public final class Validaciones {

    private Validaciones() {
    }

    public static <T> T requerido(T valor, String campo) {
        if (valor == null) {
            throw new ValorInvalidoException("El campo '" + campo + "' es obligatorio");
        }
        return valor;
    }

    /** Exige texto no vacío, lo recorta y controla su longitud máxima. */
    public static String textoRequerido(String valor, String campo, int longitudMaxima) {
        if (valor == null || valor.isBlank()) {
            throw new ValorInvalidoException("El campo '" + campo + "' no puede estar vacío");
        }
        String limpio = valor.strip();
        if (limpio.length() > longitudMaxima) {
            throw new ValorInvalidoException(
                    "El campo '" + campo + "' supera " + longitudMaxima + " caracteres");
        }
        return limpio;
    }

    /** Texto opcional: null o vacío se normaliza a null para no guardar cadenas vacías. */
    public static String textoOpcional(String valor, String campo, int longitudMaxima) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return textoRequerido(valor, campo, longitudMaxima);
    }
}
