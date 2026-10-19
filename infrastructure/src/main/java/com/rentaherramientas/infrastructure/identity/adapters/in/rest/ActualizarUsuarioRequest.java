package com.rentaherramientas.infrastructure.identity.adapters.in.rest;

import com.rentaherramientas.application.identity.command.ActualizarUsuarioCommand;
import com.rentaherramientas.application.identity.command.RegistrarUsuarioCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de PUT /api/usuarios/me y PUT /api/admin/usuarios/{id}. Los datos de
 * rol son opcionales; si se envían deben corresponder al rol del usuario.
 *
 * TÉCNICA AVANZADA (Bean Validation): `@Valid` en cascada
 *   Qué hace: poner `@Valid` sobre un componente que es otro objeto (`datosProveedor`) hace que también se
 *     validen sus anotaciones internas (`@NotBlank`, `@Size`...). Si el objeto es null no se valida, por eso
 *     los datos de rol son opcionales.
 */
public record ActualizarUsuarioRequest(
        @Schema(description = "Nombre completo del usuario", example = "Andrés Felipe Rueda Pinzón")
        @NotBlank @Size(max = 120) String nombre,
        @Schema(description = "Solo para usuarios PROVEEDOR; opcional")
        @Valid RegistroUsuarioRequest.DatosProveedorRequest datosProveedor,
        @Schema(description = "Solo para usuarios CLIENTE; opcional")
        @Valid RegistroUsuarioRequest.DatosClienteRequest datosCliente) {

    /** Convierte el cuerpo HTTP en el command de aplicación para el usuario indicado. */
    public ActualizarUsuarioCommand toCommand(Long usuarioId) {
        return new ActualizarUsuarioCommand(usuarioId, nombre,
                datosProveedor == null ? null
                        : new RegistrarUsuarioCommand.DatosProveedor(datosProveedor.razonSocial(), datosProveedor.nit()),
                datosCliente == null ? null
                        : new RegistrarUsuarioCommand.DatosCliente(datosCliente.documento(), datosCliente.telefono(),
                                datosCliente.direccion()));
    }
}
