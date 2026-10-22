package com.rentaherramientas.domain.identity.model.aggregate;

import com.rentaherramientas.domain.identity.exception.DatosRolInconsistentesException;
import com.rentaherramientas.domain.identity.exception.RolSinCambioException;
import com.rentaherramientas.domain.identity.model.valueobject.DatosCliente;
import com.rentaherramientas.domain.identity.model.valueobject.DatosProveedor;
import com.rentaherramientas.domain.identity.model.valueobject.Email;
import com.rentaherramientas.domain.identity.model.valueobject.PasswordHash;
import com.rentaherramientas.domain.identity.model.valueobject.Rol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del cambio de rol del agregado Usuario: reemplazo de los datos de
 * subtipo y coherencia rol ↔ datos.
 */
class UsuarioTest {

    private static final DatosCliente DATOS_CLIENTE =
            new DatosCliente("1098654321", "3157894561", "Cra 33 # 48-25, Bucaramanga");
    private static final DatosProveedor DATOS_PROVEEDOR =
            new DatosProveedor("Herramientas Rueda SAS", "901234567-8");

    private static Usuario cliente() {
        return Usuario.reconstruir(22L, "Andrés Rueda", new Email("andres.rueda@gmail.com"),
                new PasswordHash("$2a$10$hash"), Rol.CLIENTE, true, null, DATOS_CLIENTE);
    }

    @Test
    @DisplayName("CLIENTE → PROVEEDOR con datos de proveedor: cambia el rol y descarta los datos de cliente")
    void clienteAProveedor() {
        Usuario usuario = cliente();

        usuario.cambiarRol(Rol.PROVEEDOR, DATOS_PROVEEDOR, null);

        assertEquals(Rol.PROVEEDOR, usuario.getRol());
        assertEquals(Optional.of(DATOS_PROVEEDOR), usuario.getDatosProveedor());
        assertTrue(usuario.getDatosCliente().isEmpty());
    }

    @Test
    @DisplayName("CLIENTE → PROVEEDOR sin datos de proveedor → DatosRolInconsistentesException y no cambia nada")
    void clienteAProveedorSinDatos() {
        Usuario usuario = cliente();

        assertThrows(DatosRolInconsistentesException.class, () -> usuario.cambiarRol(Rol.PROVEEDOR, null, null));
        assertEquals(Rol.CLIENTE, usuario.getRol());
        assertEquals(Optional.of(DATOS_CLIENTE), usuario.getDatosCliente());
    }

    @Test
    @DisplayName("CLIENTE → ADMIN: queda sin datos de subtipo")
    void clienteAAdmin() {
        Usuario usuario = cliente();

        usuario.cambiarRol(Rol.ADMIN, null, null);

        assertEquals(Rol.ADMIN, usuario.getRol());
        assertTrue(usuario.getDatosProveedor().isEmpty());
        assertTrue(usuario.getDatosCliente().isEmpty());
    }

    @Test
    @DisplayName("Mismo rol → RolSinCambioException")
    void mismoRol() {
        Usuario usuario = cliente();

        assertThrows(RolSinCambioException.class, () -> usuario.cambiarRol(Rol.CLIENTE, null, DATOS_CLIENTE));
    }
}
