package com.rentaherramientas.application.identity.usecase;

import com.rentaherramientas.application.identity.command.LoginCommand;
import com.rentaherramientas.application.identity.dto.TokenResponse;
import com.rentaherramientas.application.identity.exception.CredencialesInvalidasException;
import com.rentaherramientas.application.identity.port.CodificadorPassword;
import com.rentaherramientas.application.identity.port.GeneradorToken;
import com.rentaherramientas.application.identity.port.TokenEmitido;
import com.rentaherramientas.domain.identity.exception.UsuarioInactivoException;
import com.rentaherramientas.domain.identity.model.aggregate.Usuario;
import com.rentaherramientas.domain.identity.model.valueobject.Email;
import com.rentaherramientas.domain.identity.port.repository.UsuarioRepository;
import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;

/**
 * Autentica a un usuario con email y password y le emite un token. El password
 * se verifica ANTES que el estado activo: así quien no conoce la contraseña no
 * puede averiguar qué cuentas existen y están desactivadas.
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
        Usuario usuario = usuarioRepository.buscarPorEmail(emailOCredencialesInvalidas(command.email()))
                .orElseThrow(CredencialesInvalidasException::new);
        if (!codificadorPassword.coincide(command.password(), usuario.getPasswordHash())) {
            throw new CredencialesInvalidasException();
        }
        if (!usuario.isActivo()) {
            throw new UsuarioInactivoException(usuario.getId());
        }
        TokenEmitido tokenEmitido = generadorToken.generar(usuario);
        return new TokenResponse(tokenEmitido.token(), tokenEmitido.expiraEn(), usuario.getRol());
    }

    /** Un email mal formado no puede estar registrado: se responde igual que si no existiera. */
    private static Email emailOCredencialesInvalidas(String email) {
        try {
            return new Email(email);
        } catch (ValorInvalidoException e) {
            throw new CredencialesInvalidasException();
        }
    }
}
