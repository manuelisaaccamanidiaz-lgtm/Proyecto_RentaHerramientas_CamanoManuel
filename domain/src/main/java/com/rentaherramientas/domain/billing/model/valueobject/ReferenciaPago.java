package com.rentaherramientas.domain.billing.model.valueobject;

import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;

import java.util.regex.Pattern;

/**
 * Referencia única del pago (la que entrega la pasarela o la caja). Tiene formato
 * restringido para que sea segura de mostrar y buscar; su unicidad global la
 * garantiza uq_pagos_referencia.
 */
public record ReferenciaPago(String valor) {

    private static final Pattern FORMATO = Pattern.compile("^[A-Za-z0-9-]{4,60}$");

    public ReferenciaPago {
        if (valor == null || !FORMATO.matcher(valor.strip()).matches()) {
            throw new ValorInvalidoException(
                    "La referencia de pago debe tener de 4 a 60 caracteres alfanuméricos o guiones");
        }
        valor = valor.strip();
    }
}
