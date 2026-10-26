package com.rentaherramientas.application.identity.usecase;

import com.rentaherramientas.application.identity.command.CambiarPasswordCommand;
import com.rentaherramientas.application.identity.exception.PasswordActualIncorrectaException;
import com.rentaherramientas.application.identity.exception.PasswordSinCambioException;
import com.rentaherramientas.application.identity.exception.UsuarioNoEncontradoException;
import com.rentaherramientas.application.identity.port.CodificadorPassword;
import com.rentaherramientas.domain.identity.model.aggregate.Usuario;
import com.rentaherramientas.domain.identity.port.repository.UsuarioRepository;

/**
 * Cambia la contraseña del usuario autenticado. Las comparaciones con la
 * contraseña guardada usan el mismo CodificadorPassword que el login; que el
 * usuario esté activo lo exige el agregado en cambiarPassword.
 */
public class CambiarPasswordService implements CambiarPasswordUseCase {

    private final UsuarioRepository usuarioRepository;
    private final CodificadorPassword codificadorPassword;

    /** Recibe sus dependencias por constructor; el bean lo crea IdentityBeansConfig. */
    public CambiarPasswordService(UsuarioRepository usuarioRepository, CodificadorPassword codificadorPassword) {
        this.usuarioRepository = usuarioRepository;
        this.codificadorPassword = codificadorPassword;
    }

    /**
     * La nueva se compara contra el hash (no con equals sobre la actual) para
     * respetar exactamente lo que BCrypt considera "la misma contraseña"
     * (p. ej. trunca a 72 bytes).
     */
    @Override
    public void cambiarPassword(CambiarPasswordCommand command) {
        Usuario usuario = usuarioRepository.buscarPorId(command.usuarioId())
                .orElseThrow(() -> new UsuarioNoEncontradoException(command.usuarioId()));
        if (!codificadorPassword.coincide(command.passwordActual(), usuario.getPasswordHash())) {
            throw new PasswordActualIncorrectaException();
        }
        if (codificadorPassword.coincide(command.passwordNueva(), usuario.getPasswordHash())) {
            throw new PasswordSinCambioException();
        }
        usuario.cambiarPassword(codificadorPassword.codificar(command.passwordNueva()));
        usuarioRepository.guardar(usuario);
    }
}
