package com.rentaherramientas.domain.identity.model.valueobject;

import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;
import com.rentaherramientas.domain.shared.model.Validaciones;

import java.util.regex.Pattern;

/**
 * Datos que solo tienen sentido para usuarios con rol PROVEEDOR. Se modelan como
 * value object dentro de Usuario y se persisten en la tabla 1:1 "proveedores"
 * para no dejar columnas nulas en "usuarios" para clientes y administradores.
 */
public record DatosProveedor(String razonSocial, String nit) {

    private static final int RAZON_SOCIAL_MAX = 150;
    private static final int NIT_MAX = 20;
    /** NIT colombiano: 6 a 10 dígitos con dígito de verificación opcional (900123456-7). */
    private static final Pattern FORMATO_NIT = Pattern.compile("^[0-9]{6,10}(-[0-9])?$");

    /** Constructor compacto: recorta y valida razón social y el formato del NIT. */
    public DatosProveedor {
        razonSocial = Validaciones.textoRequerido(razonSocial, "razonSocial", RAZON_SOCIAL_MAX);
        nit = Validaciones.textoRequerido(nit, "nit", NIT_MAX);
        if (!FORMATO_NIT.matcher(nit).matches()) {
            throw new ValorInvalidoException("El NIT no tiene un formato válido: " + nit);
        }
    }
}
