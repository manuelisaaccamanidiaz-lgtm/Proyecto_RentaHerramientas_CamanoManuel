package com.rentaherramientas.infrastructure.identity.adapters.in.rest;

import com.rentaherramientas.application.identity.command.ActualizarUsuarioCommand;
import com.rentaherramientas.application.identity.command.RegistrarUsuarioCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de PUT /api/usuarios/me y PUT /api/admin/usuarios/{id}. Los datos de
 * rol son opcionales; si se envían deben corresponder al rol del usuario.
 */
public record ActualizarUsuarioRequest(
        @NotBlank @Size(max = 120) String nombre,
        @Valid RegistroUsuarioRequest.DatosProveedorRequest datosProveedor,
        @Valid RegistroUsuarioRequest.DatosClienteRequest datosCliente) {

    public ActualizarUsuarioCommand toCommand(Long usuarioId) {
        return new ActualizarUsuarioCommand(usuarioId, nombre,
                datosProveedor == null ? null
                        : new RegistrarUsuarioCommand.DatosProveedor(datosProveedor.razonSocial(), datosProveedor.nit()),
                datosCliente == null ? null
                        : new RegistrarUsuarioCommand.DatosCliente(datosCliente.documento(), datosCliente.telefono(),
                                datosCliente.direccion()));
    }
}
