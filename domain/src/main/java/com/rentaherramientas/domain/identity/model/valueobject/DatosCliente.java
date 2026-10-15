package com.rentaherramientas.domain.identity.model.valueobject;

import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;
import com.rentaherramientas.domain.shared.model.Validaciones;

import java.util.regex.Pattern;

/**
 * Datos que solo tienen sentido para usuarios con rol CLIENTE. Igual que
 * DatosProveedor, viven como value object dentro de Usuario y se persisten en
 * la tabla 1:1 "clientes". Las validaciones replican los CHECK de esa tabla
 * para que el error se detecte en el dominio y no al hacer INSERT.
 */
public record DatosCliente(String documento, String telefono, String direccion) {

    private static final int DOCUMENTO_MAX = 20;
    private static final int TELEFONO_MAX = 20;
    private static final int DIRECCION_MAX = 200;
    /** Igual que ck_clientes_documento_formato. */
    private static final Pattern FORMATO_DOCUMENTO = Pattern.compile("^[0-9]{5,15}$");
    /** Igual que ck_clientes_telefono_formato: "+" opcional y 7 a 15 dígitos. */
    private static final Pattern FORMATO_TELEFONO = Pattern.compile("^\\+?[0-9]{7,15}$");

    public DatosCliente {
        documento = Validaciones.textoRequerido(documento, "documento", DOCUMENTO_MAX);
        if (!FORMATO_DOCUMENTO.matcher(documento).matches()) {
            throw new ValorInvalidoException("El documento debe tener de 5 a 15 dígitos: " + documento);
        }
        telefono = Validaciones.textoRequerido(telefono, "telefono", TELEFONO_MAX);
        if (!FORMATO_TELEFONO.matcher(telefono).matches()) {
            throw new ValorInvalidoException("El teléfono no tiene un formato válido: " + telefono);
        }
        direccion = Validaciones.textoRequerido(direccion, "direccion", DIRECCION_MAX);
    }
}
