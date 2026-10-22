package com.rentaherramientas.domain.identity.model.aggregate;

import com.rentaherramientas.domain.identity.event.UsuarioDesactivado;
import com.rentaherramientas.domain.identity.event.UsuarioRegistrado;
import com.rentaherramientas.domain.identity.exception.DatosRolInconsistentesException;
import com.rentaherramientas.domain.identity.exception.RolSinCambioException;
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
 * negocio que valida la regla correspondiente. El rol solo cambia con cambiarRol,
 * que reemplaza a la vez los datos del subtipo; que el usuario no tenga historial
 * en otros BC (herramientas de un proveedor, reservas de un cliente) lo verifica
 * el caso de uso. Los datos propios de cada rol son coherentes con él siempre.
 *
 * TÉCNICA AVANZADA (DDD): constructor privado + métodos fábrica
 *   Qué hace: no se puede hacer `new Usuario(...)`. Hay dos formas de obtener uno: `registrar` (alta nueva,
 *     emite evento) y `reconstruir` (carga desde la BD, sin eventos). Así cada camino aplica sus reglas.
 */
public final class Usuario extends AggregateRoot {

    private static final int NOMBRE_MAX = 120;

    private final Long id;
    private String nombre;
    private Email email;
    private PasswordHash passwordHash;
    private Rol rol;
    private boolean activo;
    private DatosProveedor datosProveedor;
    private DatosCliente datosCliente;

    /** Constructor único y privado: valida todos los campos y la coherencia entre rol y datos de rol. */
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

    /** Cambia el nombre del usuario; exige que esté activo y que el nombre sea válido. */
    public void renombrar(String nuevoNombre) {
        exigirActivo();
        this.nombre = Validaciones.textoRequerido(nuevoNombre, "nombre", NOMBRE_MAX);
    }

    /** La unicidad del email entre usuarios la verifica el caso de uso con el repositorio. */
    public void cambiarEmail(Email nuevoEmail) {
        exigirActivo();
        this.email = Validaciones.requerido(nuevoEmail, "email");
    }

    /** Reemplaza el hash de la contraseña (la contraseña ya llega cifrada desde aplicación). */
    public void cambiarPassword(PasswordHash nuevoHash) {
        exigirActivo();
        this.passwordHash = Validaciones.requerido(nuevoHash, "passwordHash");
    }

    /** Actualiza razón social y NIT; solo permitido si el usuario es PROVEEDOR y está activo. */
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

    /**
     * Cambia el rol y reemplaza los datos de subtipo: se pasan los del nuevo rol y
     * null en el otro (ADMIN: ambos null). Los datos del rol anterior se descartan.
     * Se valida antes de asignar para que un error no deje el agregado a medias.
     */
    public void cambiarRol(Rol nuevoRol, DatosProveedor nuevosDatosProveedor, DatosCliente nuevosDatosCliente) {
        Validaciones.requerido(nuevoRol, "rol");
        if (nuevoRol == rol) {
            throw new RolSinCambioException(id, rol);
        }
        validarCoherenciaRol(nuevoRol, nuevosDatosProveedor, nuevosDatosCliente);
        this.rol = nuevoRol;
        this.datosProveedor = nuevosDatosProveedor;
        this.datosCliente = nuevosDatosCliente;
    }

    /** Idempotente: desactivar un usuario ya inactivo no emite un segundo evento. */
    public void desactivar() {
        if (!activo) {
            return;
        }
        this.activo = false;
        registrarEvento(new UsuarioDesactivado(id, LocalDateTime.now()));
    }

    /** Reactiva al usuario; es idempotente y no emite eventos. */
    public void activar() {
        this.activo = true;
    }

    /** Indica si el usuario tiene rol PROVEEDOR. */
    public boolean esProveedor() {
        return rol == Rol.PROVEEDOR;
    }

    /** Indica si el usuario tiene rol CLIENTE. */
    public boolean esCliente() {
        return rol == Rol.CLIENTE;
    }

    /** Guarda de seguridad usada por los métodos de modificación: lanza excepción si está inactivo. */
    private void exigirActivo() {
        if (!activo) {
            throw new UsuarioInactivoException(id);
        }
    }

    /**
     * PROVEEDOR → solo DatosProveedor; CLIENTE → solo DatosCliente; ADMIN → ninguno.
     *
     * TÉCNICA AVANZADA (Java): switch expression sobre enum
     *   Qué hace: `switch (rol) { case X -> valor; }` devuelve un valor y, al cubrir todas las constantes del
     *     enum, no necesita `default`: si mañana se añade un rol nuevo, el código deja de compilar hasta
     *     tratarlo.
     *   Cómo usarlo: `var r = switch (e) { case A -> 1; case B -> 2; };`
     */
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

    /** Identificador asignado por la base de datos; null mientras el usuario no se ha guardado. */
    public Long getId() {
        return id;
    }

    /** Nombre visible del usuario. */
    public String getNombre() {
        return nombre;
    }

    /** Email normalizado, usado como nombre de inicio de sesión. */
    public Email getEmail() {
        return email;
    }

    /** Hash de la contraseña (nunca la contraseña en claro). */
    public PasswordHash getPasswordHash() {
        return passwordHash;
    }

    /** Rol del usuario; solo lo modifica cambiarRol. */
    public Rol getRol() {
        return rol;
    }

    /** Indica si el usuario puede iniciar sesión y modificar sus datos. */
    public boolean isActivo() {
        return activo;
    }

    /**
     * Datos de proveedor si el usuario es PROVEEDOR; vacío en otro caso.
     *
     * TÉCNICA AVANZADA (Java): `Optional` como retorno
     *   Qué hace: obliga a quien llama a pensar en el caso "no hay valor" en vez de recibir un null
     *     inesperado.
     *   Cómo usarlo: `usuario.getDatosProveedor().map(DatosProveedor::nit).orElse("-")`.
     */
    public Optional<DatosProveedor> getDatosProveedor() {
        return Optional.ofNullable(datosProveedor);
    }

    /** Datos de cliente si el usuario es CLIENTE; vacío en otro caso. */
    public Optional<DatosCliente> getDatosCliente() {
        return Optional.ofNullable(datosCliente);
    }
}
