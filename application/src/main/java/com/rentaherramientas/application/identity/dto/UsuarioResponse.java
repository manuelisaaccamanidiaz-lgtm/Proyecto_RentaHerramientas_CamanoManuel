package com.rentaherramientas.application.identity.dto;

import com.rentaherramientas.domain.identity.model.aggregate.Usuario;
import com.rentaherramientas.domain.identity.model.valueobject.Rol;

/**
 * Vista pública de un usuario. Nunca incluye el hash de la contraseña. Según el
 * rol, datosProveedor o datosCliente van informados y el otro en null.
 */
public record UsuarioResponse(Long id, String nombre, String email, Rol rol, boolean activo,
                              DatosProveedorResponse datosProveedor, DatosClienteResponse datosCliente) {

    public record DatosProveedorResponse(String razonSocial, String nit) {
    }

    public record DatosClienteResponse(String documento, String telefono, String direccion) {
    }

    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail().valor(),
                usuario.getRol(),
                usuario.isActivo(),
                usuario.getDatosProveedor()
                        .map(datos -> new DatosProveedorResponse(datos.razonSocial(), datos.nit()))
                        .orElse(null),
                usuario.getDatosCliente()
                        .map(datos -> new DatosClienteResponse(datos.documento(), datos.telefono(), datos.direccion()))
                        .orElse(null));
    }
}
