package com.rentaherramientas.domain.identity.model.valueobject;

import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Correo electrónico validado y normalizado a minúsculas. Normalizar aquí evita
 * que "Ana@x.com" y "ana@x.com" se registren como usuarios distintos.
 */
public record Email(String valor) {

    private static final int LONGITUD_MAXIMA = 150;
    private static final Pattern FORMATO =
            Pattern.compile("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$");

    /**
     * Constructor compacto: rechaza vacíos, normaliza a minúsculas y valida longitud y formato.
     *
     * TÉCNICA AVANZADA (Java): `Locale.ROOT` en `toLowerCase`
     *   Qué hace: sin él, en equipos configurados en turco la "I" se convierte en "ı" y el email cambiaría.
     *     ROOT da un resultado independiente del idioma del sistema.
     */
    public Email {
        if (valor == null || valor.isBlank()) {
            throw new ValorInvalidoException("El email es obligatorio");
        }
        valor = valor.strip().toLowerCase(Locale.ROOT);
        if (valor.length() > LONGITUD_MAXIMA) {
            throw new ValorInvalidoException("El email supera " + LONGITUD_MAXIMA + " caracteres");
        }
        if (!FORMATO.matcher(valor).matches()) {
            throw new ValorInvalidoException("El email no tiene un formato válido: " + valor);
        }
    }
}
