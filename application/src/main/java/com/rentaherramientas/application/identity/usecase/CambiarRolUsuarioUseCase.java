package com.rentaherramientas.application.identity.usecase;

import com.rentaherramientas.application.identity.command.CambiarRolUsuarioCommand;
import com.rentaherramientas.application.identity.dto.UsuarioResponse;

/**
 * Cambia el rol de un usuario (solo ADMIN). El rol viaja en el JWT, así que el
 * usuario afectado debe volver a iniciar sesión para que el cambio surta efecto.
 */
public interface CambiarRolUsuarioUseCase {

    /** Cambia el rol y los datos de subtipo; devuelve el usuario actualizado. */
    UsuarioResponse cambiarRol(CambiarRolUsuarioCommand command);
}
