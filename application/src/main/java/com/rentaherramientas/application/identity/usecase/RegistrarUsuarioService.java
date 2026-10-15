package com.rentaherramientas.application.identity.usecase;

import com.rentaherramientas.application.identity.command.RegistrarUsuarioCommand;
import com.rentaherramientas.application.identity.dto.UsuarioResponse;
import com.rentaherramientas.application.identity.exception.EmailYaRegistradoException;
import com.rentaherramientas.application.identity.exception.RolNoPermitidoException;
import com.rentaherramientas.application.identity.port.CodificadorPassword;
import com.rentaherramientas.domain.identity.model.aggregate.Usuario;
import com.rentaherramientas.domain.identity.model.valueobject.DatosCliente;
import com.rentaherramientas.domain.identity.model.valueobject.DatosProveedor;
import com.rentaherramientas.domain.identity.model.valueobject.Email;
import com.rentaherramientas.domain.identity.model.valueobject.PasswordHash;
import com.rentaherramientas.domain.identity.model.valueobject.Rol;
import com.rentaherramientas.domain.identity.port.repository.UsuarioRepository;

/**
 * Registra un CLIENTE o un PROVEEDOR. Los value objects se construyen antes de
 * codificar el password para no gastar un hash BCrypt en datos inválidos. La
 * unicidad del email se verifica aquí; la restricción UNIQUE de la BD cubre la
 * carrera entre dos registros simultáneos.
 */
public class RegistrarUsuarioService implements RegistrarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final CodificadorPassword codificadorPassword;

    public RegistrarUsuarioService(UsuarioRepository usuarioRepository, CodificadorPassword codificadorPassword) {
        this.usuarioRepository = usuarioRepository;
        this.codificadorPassword = codificadorPassword;
    }

    @Override
    public UsuarioResponse registrar(RegistrarUsuarioCommand command) {
        if (command.rol() != Rol.CLIENTE && command.rol() != Rol.PROVEEDOR) {
            throw new RolNoPermitidoException(command.rol());
        }
        Email email = new Email(command.email());
        if (usuarioRepository.existePorEmail(email)) {
            throw new EmailYaRegistradoException(email.valor());
        }
        DatosProveedor datosProveedor = command.datosProveedor() == null
                ? null
                : new DatosProveedor(command.datosProveedor().razonSocial(), command.datosProveedor().nit());
        DatosCliente datosCliente = command.datosCliente() == null
                ? null
                : new DatosCliente(command.datosCliente().documento(), command.datosCliente().telefono(),
                        command.datosCliente().direccion());

        PasswordHash passwordHash = codificadorPassword.codificar(command.password());
        Usuario usuario = Usuario.registrar(command.nombre(), email, passwordHash, command.rol(),
                datosProveedor, datosCliente);
        return UsuarioResponse.desde(usuarioRepository.guardar(usuario));
    }
}
