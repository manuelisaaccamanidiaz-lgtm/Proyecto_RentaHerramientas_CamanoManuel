package com.rentaherramientas.infrastructure.identity.adapters.in.rest;

import com.rentaherramientas.application.identity.command.CambiarRolUsuarioCommand;
import com.rentaherramientas.application.identity.command.RegistrarUsuarioCommand;
import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.domain.identity.model.valueobject.Rol;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo de PATCH /api/admin/usuarios/{id}/rol. Solo valida forma; qué bloque de
 * datos corresponde a cada rol lo decide el agregado Usuario.
 */
public record CambiarRolRequest(
        @Schema(description = "Nuevo rol del usuario", example = "PROVEEDOR")
        @NotNull Rol rol,
        @Schema(description = "Obligatorio si rol = PROVEEDOR; omitir en otro caso")
        @Valid RegistroUsuarioRequest.DatosProveedorRequest datosProveedor,
        @Schema(description = "Obligatorio si rol = CLIENTE; omitir en otro caso")
        @Valid RegistroUsuarioRequest.DatosClienteRequest datosCliente) {

    /** Convierte el cuerpo HTTP en el command de aplicación. */
    public CambiarRolUsuarioCommand toCommand(Long usuarioId, Solicitante solicitante) {
        return new CambiarRolUsuarioCommand(solicitante, usuarioId, rol,
                datosProveedor == null ? null
                        : new RegistrarUsuarioCommand.DatosProveedor(datosProveedor.razonSocial(), datosProveedor.nit()),
                datosCliente == null ? null
                        : new RegistrarUsuarioCommand.DatosCliente(datosCliente.documento(), datosCliente.telefono(),
                                datosCliente.direccion()));
    }
}
