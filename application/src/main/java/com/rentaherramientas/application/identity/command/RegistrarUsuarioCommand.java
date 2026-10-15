package com.rentaherramientas.application.identity.command;

import com.rentaherramientas.domain.identity.model.valueobject.Rol;

/**
 * Intención de registrar un usuario. Según el rol se envían los datos de
 * proveedor o los de cliente (el otro va en null); la coherencia rol ↔ datos
 * la valida el agregado Usuario. El password viaja en claro solo hasta el caso
 * de uso, que lo codifica antes de crear el agregado.
 */
public record RegistrarUsuarioCommand(String nombre, String email, String password, Rol rol,
                                      DatosProveedor datosProveedor, DatosCliente datosCliente) {

    public record DatosProveedor(String razonSocial, String nit) {
    }

    public record DatosCliente(String documento, String telefono, String direccion) {
    }

    @Override
    public String toString() {
        return "RegistrarUsuarioCommand[nombre=" + nombre + ", email=" + email + ", password=****, rol=" + rol + "]";
    }
}
