package com.rentaherramientas.application.identity.usecase;

import com.rentaherramientas.application.identity.command.CambiarRolUsuarioCommand;
import com.rentaherramientas.application.identity.command.RegistrarUsuarioCommand;
import com.rentaherramientas.application.identity.dto.UsuarioResponse;
import com.rentaherramientas.application.identity.exception.UsuarioConHistorialException;
import com.rentaherramientas.application.identity.port.ActividadUsuario;
import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.application.shared.exception.AccesoDenegadoException;
import com.rentaherramientas.domain.identity.model.aggregate.Usuario;
import com.rentaherramientas.domain.identity.model.valueobject.DatosCliente;
import com.rentaherramientas.domain.identity.model.valueobject.DatosProveedor;
import com.rentaherramientas.domain.identity.model.valueobject.Email;
import com.rentaherramientas.domain.identity.model.valueobject.PasswordHash;
import com.rentaherramientas.domain.identity.model.valueobject.Rol;
import com.rentaherramientas.domain.identity.port.repository.UsuarioRepository;
import com.rentaherramientas.domain.shared.model.Pagina;
import com.rentaherramientas.domain.shared.model.SolicitudPagina;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas del caso de uso con dobles escritos a mano (sin Mockito ni Spring):
 * un repositorio de usuarios en memoria y un registro de actividad configurable.
 */
class CambiarRolUsuarioServiceTest {

    private static final Solicitante ADMIN = new Solicitante(1L, Rol.ADMIN);
    private static final Long CLIENTE_ID = 22L;
    private static final Long PROVEEDOR_ID = 23L;
    private static final RegistrarUsuarioCommand.DatosProveedor DATOS_PROVEEDOR =
            new RegistrarUsuarioCommand.DatosProveedor("Herramientas Rueda SAS", "901234567-8");
    private static final RegistrarUsuarioCommand.DatosCliente DATOS_CLIENTE =
            new RegistrarUsuarioCommand.DatosCliente("1098654321", "3157894561", "Cra 33 # 48-25, Bucaramanga");

    private UsuarioRepositoryEnMemoria usuarios;
    private ActividadUsuarioFalsa actividad;
    private CambiarRolUsuarioService service;

    @BeforeEach
    void preparar() {
        usuarios = new UsuarioRepositoryEnMemoria();
        usuarios.agregar(Usuario.reconstruir(ADMIN.id(), "Admin", new Email("admin@renta.test"),
                new PasswordHash("$2a$10$hash"), Rol.ADMIN, true, null, null));
        usuarios.agregar(Usuario.reconstruir(CLIENTE_ID, "Andrés Rueda", new Email("andres@renta.test"),
                new PasswordHash("$2a$10$hash"), Rol.CLIENTE, true, null,
                new DatosCliente("1098654321", "3157894561", "Cra 33 # 48-25, Bucaramanga")));
        usuarios.agregar(Usuario.reconstruir(PROVEEDOR_ID, "Carlos Pinzón", new Email("carlos@renta.test"),
                new PasswordHash("$2a$10$hash"), Rol.PROVEEDOR, true,
                new DatosProveedor("Ferretería El Tornillo SAS", "900101001-3"), null));
        actividad = new ActividadUsuarioFalsa();
        service = new CambiarRolUsuarioService(usuarios, actividad);
    }

    @Test
    @DisplayName("Un ADMIN no puede cambiar su propio rol → AccesoDenegadoException (403)")
    void adminNoCambiaSuPropioRol() {
        CambiarRolUsuarioCommand command = new CambiarRolUsuarioCommand(ADMIN, ADMIN.id(), Rol.CLIENTE,
                null, DATOS_CLIENTE);

        assertThrows(AccesoDenegadoException.class, () -> service.cambiarRol(command));
        assertEquals(0, usuarios.guardados);
    }

    @Test
    @DisplayName("CLIENTE con reservas → UsuarioConHistorialException (409) y no guarda")
    void clienteConReservas() {
        actividad.clientesConReservas.add(CLIENTE_ID);
        CambiarRolUsuarioCommand command = new CambiarRolUsuarioCommand(ADMIN, CLIENTE_ID, Rol.PROVEEDOR,
                DATOS_PROVEEDOR, null);

        assertThrows(UsuarioConHistorialException.class, () -> service.cambiarRol(command));
        assertEquals(0, usuarios.guardados);
    }

    @Test
    @DisplayName("PROVEEDOR con herramientas → UsuarioConHistorialException (409) y no guarda")
    void proveedorConHerramientas() {
        actividad.proveedoresConHerramientas.add(PROVEEDOR_ID);
        CambiarRolUsuarioCommand command = new CambiarRolUsuarioCommand(ADMIN, PROVEEDOR_ID, Rol.ADMIN, null, null);

        assertThrows(UsuarioConHistorialException.class, () -> service.cambiarRol(command));
        assertEquals(0, usuarios.guardados);
    }

    @Test
    @DisplayName("Caso feliz: CLIENTE sin historial → PROVEEDOR, se guarda con los nuevos datos")
    void casoFeliz() {
        CambiarRolUsuarioCommand command = new CambiarRolUsuarioCommand(ADMIN, CLIENTE_ID, Rol.PROVEEDOR,
                DATOS_PROVEEDOR, null);

        UsuarioResponse respuesta = service.cambiarRol(command);

        assertEquals(Rol.PROVEEDOR, respuesta.rol());
        assertEquals("901234567-8", respuesta.datosProveedor().nit());
        assertNull(respuesta.datosCliente());
        assertEquals(1, usuarios.guardados);
        assertEquals(Rol.PROVEEDOR, usuarios.buscarPorId(CLIENTE_ID).orElseThrow().getRol());
    }

    // ------------------------------------------------------- dobles de prueba

    /** Repositorio en memoria que cuenta cuántas veces se guardó. */
    private static final class UsuarioRepositoryEnMemoria implements UsuarioRepository {

        private final Map<Long, Usuario> porId = new HashMap<>();
        private int guardados;

        void agregar(Usuario usuario) {
            porId.put(usuario.getId(), usuario);
        }

        @Override
        public Usuario guardar(Usuario usuario) {
            guardados++;
            porId.put(usuario.getId(), usuario);
            return usuario;
        }

        @Override
        public Optional<Usuario> buscarPorId(Long id) {
            return Optional.ofNullable(porId.get(id));
        }

        @Override
        public Optional<Usuario> buscarPorEmail(Email email) {
            return porId.values().stream().filter(u -> u.getEmail().equals(email)).findFirst();
        }

        @Override
        public boolean existePorEmail(Email email) {
            return buscarPorEmail(email).isPresent();
        }

        @Override
        public Pagina<Usuario> buscar(Rol rol, SolicitudPagina pagina) {
            List<Usuario> filtrados = porId.values().stream().filter(u -> rol == null || u.getRol() == rol).toList();
            return new Pagina<>(filtrados, pagina.numero(), pagina.tamano(), filtrados.size());
        }
    }

    /** Actividad configurable: ids de clientes con reservas y de proveedores con herramientas. */
    private static final class ActividadUsuarioFalsa implements ActividadUsuario {

        private final Set<Long> clientesConReservas = new HashSet<>();
        private final Set<Long> proveedoresConHerramientas = new HashSet<>();

        @Override
        public boolean tieneReservasComoCliente(Long usuarioId) {
            return clientesConReservas.contains(usuarioId);
        }

        @Override
        public boolean tieneHerramientasComoProveedor(Long usuarioId) {
            return proveedoresConHerramientas.contains(usuarioId);
        }
    }
}
