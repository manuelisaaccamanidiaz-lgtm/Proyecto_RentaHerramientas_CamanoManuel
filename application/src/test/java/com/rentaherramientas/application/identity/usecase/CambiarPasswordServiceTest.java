package com.rentaherramientas.application.identity.usecase;

import com.rentaherramientas.application.identity.command.CambiarPasswordCommand;
import com.rentaherramientas.application.identity.exception.PasswordActualIncorrectaException;
import com.rentaherramientas.application.identity.exception.PasswordSinCambioException;
import com.rentaherramientas.application.identity.port.CodificadorPassword;
import com.rentaherramientas.domain.identity.model.aggregate.Usuario;
import com.rentaherramientas.domain.identity.model.valueobject.DatosCliente;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del caso de uso con dobles escritos a mano (sin Mockito ni Spring):
 * un repositorio de usuarios en memoria y un codificador que "hashea"
 * anteponiendo un prefijo, suficiente para comprobar qué se compara y guarda.
 */
class CambiarPasswordServiceTest {

    private static final Long USUARIO_ID = 22L;
    private static final String ACTUAL = "Clave123*";
    private static final String NUEVA = "NuevaClave456*";

    private UsuarioRepositoryEnMemoria usuarios;
    private CodificadorPassword codificador;
    private CambiarPasswordService service;

    @BeforeEach
    void preparar() {
        codificador = new CodificadorFalso();
        usuarios = new UsuarioRepositoryEnMemoria();
        usuarios.agregar(Usuario.reconstruir(USUARIO_ID, "Andrés Rueda", new Email("andres@renta.test"),
                codificador.codificar(ACTUAL), Rol.CLIENTE, true, null,
                new DatosCliente("1098654321", "3157894561", "Cra 33 # 48-25, Bucaramanga")));
        service = new CambiarPasswordService(usuarios, codificador);
    }

    @Test
    @DisplayName("Contraseña actual incorrecta → PasswordActualIncorrectaException (400) y no guarda")
    void actualIncorrecta() {
        CambiarPasswordCommand command = new CambiarPasswordCommand(USUARIO_ID, "OtraClave*", NUEVA);

        assertThrows(PasswordActualIncorrectaException.class, () -> service.cambiarPassword(command));
        assertEquals(0, usuarios.guardados);
    }

    @Test
    @DisplayName("Nueva igual a la actual → PasswordSinCambioException (400) y no guarda")
    void nuevaIgualActual() {
        CambiarPasswordCommand command = new CambiarPasswordCommand(USUARIO_ID, ACTUAL, ACTUAL);

        assertThrows(PasswordSinCambioException.class, () -> service.cambiarPassword(command));
        assertEquals(0, usuarios.guardados);
    }

    @Test
    @DisplayName("Caso feliz: guarda el hash de la nueva y la actual deja de coincidir")
    void casoFeliz() {
        service.cambiarPassword(new CambiarPasswordCommand(USUARIO_ID, ACTUAL, NUEVA));

        PasswordHash guardado = usuarios.buscarPorId(USUARIO_ID).orElseThrow().getPasswordHash();
        assertEquals(1, usuarios.guardados);
        assertTrue(codificador.coincide(NUEVA, guardado));
        assertFalse(codificador.coincide(ACTUAL, guardado));
    }

    // ------------------------------------------------------- dobles de prueba

    /** "Hash" reversible y determinista: basta para las pruebas, nunca para producción. */
    private static final class CodificadorFalso implements CodificadorPassword {

        @Override
        public PasswordHash codificar(String plano) {
            return new PasswordHash("hash:" + plano);
        }

        @Override
        public boolean coincide(String plano, PasswordHash hash) {
            return hash.valor().equals("hash:" + plano);
        }
    }

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
}
