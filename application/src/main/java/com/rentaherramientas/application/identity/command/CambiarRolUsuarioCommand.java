package com.rentaherramientas.application.identity.command;

import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.domain.identity.model.valueobject.Rol;

/**
 * Intención de un ADMIN de cambiar el rol de un usuario. Según el nuevo rol se
 * envían los datos de proveedor o los de cliente (el otro en null; ADMIN ambos
 * en null); la coherencia rol ↔ datos la valida el agregado Usuario.
 */
public record CambiarRolUsuarioCommand(Solicitante solicitante, Long usuarioId, Rol rol,
                                       RegistrarUsuarioCommand.DatosProveedor datosProveedor,
                                       RegistrarUsuarioCommand.DatosCliente datosCliente) {
}
