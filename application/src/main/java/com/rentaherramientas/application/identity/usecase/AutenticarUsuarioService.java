package com.rentaherramientas.application.identity.usecase;

import com.rentaherramientas.application.identity.command.LoginCommand;
import com.rentaherramientas.application.identity.dto.TokenResponse;
import com.rentaherramientas.application.identity.port.CodificadorPassword;
import com.rentaherramientas.application.identity.port.GeneradorToken;
import com.rentaherramientas.domain.identity.port.repository.UsuarioRepository;

/**
 * Autentica a un usuario con email y password y le emite un token.
 */
public class AutenticarUsuarioService implements AutenticarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final CodificadorPassword codificadorPassword;
    private final GeneradorToken generadorToken;

    public AutenticarUsuarioService(UsuarioRepository usuarioRepository, CodificadorPassword codificadorPassword,
                                    GeneradorToken generadorToken) {
        this.usuarioRepository = usuarioRepository;
        this.codificadorPassword = codificadorPassword;
        this.generadorToken = generadorToken;
    }

    @Override
    public TokenResponse autenticar(LoginCommand command) {
        // TODO Isaac: implementar el login en estos 5 pasos:
        //  1. Buscar el usuario por email (new Email(command.email()) + usuarioRepository.buscarPorEmail).
        //     Si el email es inválido o no existe → CredencialesInvalidasException (no revelar cuál falló).
        //  2. Comparar el password con codificadorPassword.coincide(command.password(), usuario.getPasswordHash()).
        //     Si no coincide → CredencialesInvalidasException (mismo mensaje que en el paso 1).
        //  3. Verificar que el usuario esté activo (usuario.isActivo()); si no → UsuarioInactivoException.
        //  4. Emitir el token con generadorToken.generar(usuario) → TokenEmitido(token, expiraEn).
        //  5. Devolver new TokenResponse(tokenEmitido.token(), tokenEmitido.expiraEn(), usuario.getRol()).
        throw new UnsupportedOperationException("TODO Isaac: implementar login");
    }
}
