package com.rentaherramientas.application.billing.port;

/**
 * Persona o empresa que aparece en la factura. Para un proveedor: razón social y
 * NIT; para un cliente: nombre, cédula, teléfono y dirección (los que falten van en null).
 */
public record Tercero(Long id, String nombre, String email, String tipoDocumento, String numeroDocumento,
                      String telefono, String direccion) {
}
