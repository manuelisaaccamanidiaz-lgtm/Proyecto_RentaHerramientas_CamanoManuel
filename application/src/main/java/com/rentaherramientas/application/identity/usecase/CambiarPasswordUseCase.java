package com.rentaherramientas.application.identity.usecase;

import com.rentaherramientas.application.identity.command.CambiarPasswordCommand;

/**
 * Cambia la contraseña del usuario autenticado (cualquier rol), previa
 * verificación de la contraseña actual.
 */
public interface CambiarPasswordUseCase {

    /** Verifica la contraseña actual y guarda el hash de la nueva. */
    void cambiarPassword(CambiarPasswordCommand command);
}
