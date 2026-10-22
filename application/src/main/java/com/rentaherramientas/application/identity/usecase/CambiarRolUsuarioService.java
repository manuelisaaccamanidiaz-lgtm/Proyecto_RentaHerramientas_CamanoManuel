package com.rentaherramientas.application.identity.usecase;

import com.rentaherramientas.application.identity.command.CambiarRolUsuarioCommand;
import com.rentaherramientas.application.identity.dto.UsuarioResponse;
import com.rentaherramientas.application.identity.exception.UsuarioConHistorialException;
import com.rentaherramientas.application.identity.exception.UsuarioNoEncontradoException;
import com.rentaherramientas.application.identity.port.ActividadUsuario;
import com.rentaherramientas.application.shared.exception.AccesoDenegadoException;
import com.rentaherramientas.domain.identity.model.aggregate.Usuario;
import com.rentaherramientas.domain.identity.model.valueobject.DatosCliente;
import com.rentaherramientas.domain.identity.model.valueobject.DatosProveedor;
import com.rentaherramientas.domain.identity.model.valueobject.Rol;
import com.rentaherramientas.domain.identity.port.repository.UsuarioRepository;

/**
 * Cambia el rol de un usuario. Reglas: un ADMIN no puede cambiar su propio rol
 * (podría quedarse sin acceso a la administración) y no se cambia el rol de un
 * usuario con historial en su rol actual. La coherencia rol ↔ datos y el "mismo
 * rol" los valida el agregado.
 */
public class CambiarRolUsuarioService implements CambiarRolUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final ActividadUsuario actividadUsuario;

    /** Recibe sus dependencias por constructor; el bean lo crea IdentityBeansConfig. */
    public CambiarRolUsuarioService(UsuarioRepository usuarioRepository, ActividadUsuario actividadUsuario) {
        this.usuarioRepository = usuarioRepository;
        this.actividadUsuario = actividadUsuario;
    }

    /**
     * Primero aplica el cambio en memoria (valida mismo rol y datos sin tocar la BD) y
     * después consulta el historial del rol anterior; si algo falla no se guarda nada.
     * Si entre la consulta y el guardado aparece una reserva o herramienta, las FK de
     * reservas → clientes y herramientas → proveedores impiden borrar el subtipo (409).
     */
    @Override
    public UsuarioResponse cambiarRol(CambiarRolUsuarioCommand command) {
        if (command.usuarioId().equals(command.solicitante().id())) {
            throw new AccesoDenegadoException("Un administrador no puede cambiar su propio rol");
        }
        Usuario usuario = usuarioRepository.buscarPorId(command.usuarioId())
                .orElseThrow(() -> new UsuarioNoEncontradoException(command.usuarioId()));
        Rol rolAnterior = usuario.getRol();
        usuario.cambiarRol(command.rol(), datosProveedor(command), datosCliente(command));
        verificarSinHistorial(usuario.getId(), rolAnterior);
        return UsuarioResponse.desde(usuarioRepository.guardar(usuario));
    }

    /** Lanza UsuarioConHistorialException si el usuario dejó rastro con su rol anterior. */
    private void verificarSinHistorial(Long usuarioId, Rol rolAnterior) {
        if (rolAnterior == Rol.CLIENTE && actividadUsuario.tieneReservasComoCliente(usuarioId)) {
            throw new UsuarioConHistorialException(
                    "El usuario tiene reservas registradas: no se puede cambiar su rol");
        }
        if (rolAnterior == Rol.PROVEEDOR && actividadUsuario.tieneHerramientasComoProveedor(usuarioId)) {
            throw new UsuarioConHistorialException(
                    "El usuario tiene herramientas registradas: no se puede cambiar su rol");
        }
    }

    private static DatosProveedor datosProveedor(CambiarRolUsuarioCommand command) {
        return command.datosProveedor() == null ? null
                : new DatosProveedor(command.datosProveedor().razonSocial(), command.datosProveedor().nit());
    }

    private static DatosCliente datosCliente(CambiarRolUsuarioCommand command) {
        return command.datosCliente() == null ? null
                : new DatosCliente(command.datosCliente().documento(), command.datosCliente().telefono(),
                        command.datosCliente().direccion());
    }
}
