package com.rentaherramientas.application.identity.command;

/**
 * Intención de actualizar el nombre y los datos propios del rol de un usuario.
 * Los datos de rol son opcionales (null = no cambian); si se envían los de un
 * rol que no es el del usuario, el agregado Usuario los rechaza. Email, password
 * y rol no se modifican por esta vía.
 */
public record ActualizarUsuarioCommand(Long usuarioId, String nombre,
                                       RegistrarUsuarioCommand.DatosProveedor datosProveedor,
                                       RegistrarUsuarioCommand.DatosCliente datosCliente) {
}
