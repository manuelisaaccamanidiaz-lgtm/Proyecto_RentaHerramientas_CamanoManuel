package com.rentaherramientas.application.identity.usecase;

import com.rentaherramientas.application.identity.command.ActualizarUsuarioCommand;
import com.rentaherramientas.application.identity.dto.UsuarioResponse;
import com.rentaherramientas.application.identity.exception.UsuarioNoEncontradoException;
import com.rentaherramientas.domain.identity.model.aggregate.Usuario;
import com.rentaherramientas.domain.identity.model.valueobject.DatosCliente;
import com.rentaherramientas.domain.identity.model.valueobject.DatosProveedor;
import com.rentaherramientas.domain.identity.port.repository.UsuarioRepository;

/**
 * Actualiza el nombre y, si se envían, los datos de proveedor o de cliente. Las
 * reglas (usuario activo, datos coherentes con el rol, formatos) las aplica el
 * agregado. La unicidad de NIT y documento la garantizan las restricciones
 * UNIQUE de la BD (el adaptador REST responde 409).
 */
public class ActualizarUsuarioService implements ActualizarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;

    /** Recibe sus dependencias por constructor; el bean lo crea IdentityBeansConfig. */
    public ActualizarUsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Carga el usuario, aplica los cambios a través del agregado y lo guarda.
     *
     * TÉCNICA AVANZADA (Java): `Optional.orElseThrow(Supplier)`
     *   Qué hace: devuelve el valor si existe o lanza la excepción creada por la lambda; reemplaza el patrón
     *     "if (x == null) throw ...".
     */
    @Override
    public UsuarioResponse actualizar(ActualizarUsuarioCommand command) {
        Usuario usuario = usuarioRepository.buscarPorId(command.usuarioId())
                .orElseThrow(() -> new UsuarioNoEncontradoException(command.usuarioId()));
        usuario.renombrar(command.nombre());
        if (command.datosProveedor() != null) {
            usuario.actualizarDatosProveedor(new DatosProveedor(command.datosProveedor().razonSocial(),
                    command.datosProveedor().nit()));
        }
        if (command.datosCliente() != null) {
            usuario.actualizarDatosCliente(new DatosCliente(command.datosCliente().documento(),
                    command.datosCliente().telefono(), command.datosCliente().direccion()));
        }
        return UsuarioResponse.desde(usuarioRepository.guardar(usuario));
    }
}
