package com.rentaherramientas.domain.identity.model.aggregate;

import com.rentaherramientas.domain.identity.event.UsuarioDesactivado;
import com.rentaherramientas.domain.identity.event.UsuarioRegistrado;
import com.rentaherramientas.domain.identity.exception.DatosRolInconsistentesException;
import com.rentaherramientas.domain.identity.exception.UsuarioInactivoException;
import com.rentaherramientas.domain.identity.model.valueobject.DatosCliente;
import com.rentaherramientas.domain.identity.model.valueobject.DatosProveedor;
import com.rentaherramientas.domain.identity.model.valueobject.Email;
import com.rentaherramientas.domain.identity.model.valueobject.PasswordHash;
import com.rentaherramientas.domain.identity.model.valueobject.Rol;
import com.rentaherramientas.domain.shared.model.Validaciones;
import com.rentaherramientas.domain.shared.model.aggregate.AggregateRoot;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Raíz del agregado de identidad. Concentra las reglas sobre quién es el usuario,
 * su rol y su estado activo. No expone setters: cada cambio pasa por un método de
 * negocio que valida la regla correspondiente. El rol es inmutable porque cambiarlo
 * afectaría datos de otros BC (herramientas de un proveedor, reservas de un cliente).
 * Los datos propios de cada rol (proveedor o cliente) son coherentes con él siempre.
 */
public final class Usuario extends AggregateRoot {

    private static final int NOMBRE_MAX = 120;

    private final Long id;
    private String nombre;
    private Email email;
    private PasswordHash passwordHash;
    private final Rol rol;
    private boolean activo;
    private DatosProveedor datosProveedor;
    private DatosCliente datosCliente;

    private Usuario(Long id, String nombre, Email email, PasswordHash passwordHash, Rol rol,
                    boolean activo, DatosProveedor datosProveedor, DatosCliente datosCliente) {
        this.id = id;
        this.nombre = Validaciones.textoRequerido(nombre, "nombre", NOMBRE_MAX);
        this.email = Validaciones.requerido(email, "email");
        this.passwordHash = Validaciones.requerido(passwordHash, "passwordHash");
        this.rol = Validaciones.requerido(rol, "rol");
        this.activo = activo;
        validarCoherenciaRol(rol, datosProveedor, datosCliente);
        this.datosProveedor = datosProveedor;
        this.datosCliente = datosCliente;
    }

    /**
     * Alta de un usuario nuevo: siempre nace activo. Se pasan los datos del rol
     * que corresponda y null en el otro (ADMIN: ambos null).
     */
    public static Usuario registrar(String nombre, Email email, PasswordHash passwordHash, Rol rol,
                                    DatosProveedor datosProveedor, DatosCliente datosCliente) {
        Usuario usuario = new Usuario(null, nombre, email, passwordHash, rol, true,
                datosProveedor, datosCliente);
        usuario.registrarEvento(new UsuarioRegistrado(usuario.email.valor(), rol, LocalDateTime.now()));
        return usuario;
    }

    /** Reconstrucción desde persistencia: no emite eventos porque nada nuevo ocurrió. */
    public static Usuario reconstruir(Long id, String nombre, Email email, PasswordHash passwordHash,
                                      Rol rol, boolean activo, DatosProveedor datosProveedor,
                                      DatosCliente datosCliente) {
        return new Usuario(Validaciones.requerido(id, "id"), nombre, email, passwordHash,
                rol, activo, datosProveedor, datosCliente);
    }

    public void renombrar(String nuevoNombre) {
        exigirActivo();
        this.nombre = Validaciones.textoRequerido(nuevoNombre, "nombre", NOMBRE_MAX);
    }

    /** La unicidad del email entre usuarios la verifica el caso de uso con el repositorio. */
    public void cambiarEmail(Email nuevoEmail) {
        exigirActivo();
        this.email = Validaciones.requerido(nuevoEmail, "email");
    }

    public void cambiarPassword(PasswordHash nuevoHash) {
        exigirActivo();
        this.passwordHash = Validaciones.requerido(nuevoHash, "passwordHash");
    }

    public void actualizarDatosProveedor(DatosProveedor nuevosDatos) {
        exigirActivo();
        if (rol != Rol.PROVEEDOR) {
            throw new DatosRolInconsistentesException("Solo un PROVEEDOR tiene datos de proveedor");
        }
        this.datosProveedor = Validaciones.requerido(nuevosDatos, "datosProveedor");
    }

    /** La unicidad del documento entre clientes la verifica el caso de uso con el repositorio. */
    public void actualizarDatosCliente(DatosCliente nuevosDatos) {
        exigirActivo();
        if (rol != Rol.CLIENTE) {
            throw new DatosRolInconsistentesException("Solo un CLIENTE tiene datos de cliente");
        }
        this.datosCliente = Validaciones.requerido(nuevosDatos, "datosCliente");
    }

    /** Idempotente: desactivar un usuario ya inactivo no emite un segundo evento. */
    public void desactivar() {
        if (!activo) {
            return;
        }
        this.activo = false;
        registrarEvento(new UsuarioDesactivado(id, LocalDateTime.now()));
    }

    public void activar() {
        this.activo = true;
    }

    public boolean esProveedor() {
        return rol == Rol.PROVEEDOR;
    }

    public boolean esCliente() {
        return rol == Rol.CLIENTE;
    }

    private void exigirActivo() {
        if (!activo) {
            throw new UsuarioInactivoException(id);
        }
    }

    /** PROVEEDOR → solo DatosProveedor; CLIENTE → solo DatosCliente; ADMIN → ninguno. */
    private static void validarCoherenciaRol(Rol rol, DatosProveedor proveedor, DatosCliente cliente) {
        boolean coherente = switch (rol) {
            case PROVEEDOR -> proveedor != null && cliente == null;
            case CLIENTE -> cliente != null && proveedor == null;
            case ADMIN -> proveedor == null && cliente == null;
        };
        if (!coherente) {
            throw new DatosRolInconsistentesException(switch (rol) {
                case PROVEEDOR -> "Un PROVEEDOR debe registrar razón social y NIT, y no admite datos de cliente";
                case CLIENTE -> "Un CLIENTE debe registrar documento, teléfono y dirección, y no admite datos de proveedor";
                case ADMIN -> "Un ADMIN no admite datos de proveedor ni de cliente";
            });
        }
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Email getEmail() {
        return email;
    }

    public PasswordHash getPasswordHash() {
        return passwordHash;
    }

    public Rol getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public Optional<DatosProveedor> getDatosProveedor() {
        return Optional.ofNullable(datosProveedor);
    }

    public Optional<DatosCliente> getDatosCliente() {
        return Optional.ofNullable(datosCliente);
    }
}
