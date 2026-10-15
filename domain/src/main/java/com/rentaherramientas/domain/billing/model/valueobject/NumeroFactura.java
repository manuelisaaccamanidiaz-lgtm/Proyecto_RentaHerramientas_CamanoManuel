package com.rentaherramientas.domain.billing.model.valueobject;

import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Número consecutivo de la factura (p. ej. "FV-000123"). Se normaliza a mayúsculas
 * para que la unicidad (uq_facturas_numero) no dependa de cómo se escribió.
 */
public record NumeroFactura(String valor) {

    private static final Pattern FORMATO = Pattern.compile("^[A-Z0-9-]{1,30}$");

    public NumeroFactura {
        if (valor == null || valor.isBlank()) {
            throw new ValorInvalidoException("El número de factura es obligatorio");
        }
        valor = valor.strip().toUpperCase(Locale.ROOT);
        if (!FORMATO.matcher(valor).matches()) {
            throw new ValorInvalidoException(
                    "El número de factura solo admite letras, dígitos y guiones (máx. 30): " + valor);
        }
    }
}
