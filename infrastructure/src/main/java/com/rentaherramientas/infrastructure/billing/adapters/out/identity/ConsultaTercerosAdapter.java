package com.rentaherramientas.infrastructure.billing.adapters.out.identity;

import com.rentaherramientas.application.billing.port.ConsultaTerceros;
import com.rentaherramientas.application.billing.port.Tercero;
import com.rentaherramientas.domain.identity.model.aggregate.Usuario;
import com.rentaherramientas.domain.identity.port.repository.UsuarioRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador anticorrupción de billing hacia identity: traduce el usuario a los
 * datos que se imprimen en una factura (razón social y NIT del proveedor;
 * nombre, cédula, teléfono y dirección del cliente).
 */
@Component
public class ConsultaTercerosAdapter implements ConsultaTerceros {

    private final UsuarioRepository usuarioRepository;

    /** Recibe el repositorio de usuarios de identity por constructor. */
    public ConsultaTercerosAdapter(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /** Busca el usuario y lo traduce a los datos que se imprimen en la factura. */
    @Override
    public Optional<Tercero> buscar(Long usuarioId) {
        return usuarioRepository.buscarPorId(usuarioId).map(ConsultaTercerosAdapter::tercero);
    }

    /**
     * Proveedor: razón social y NIT. Cliente: nombre, cédula, teléfono y dirección. Otro: solo nombre y email.
     *
     * TÉCNICA AVANZADA (Java 9+): `Optional.or(...)`
     *   Qué hace: si el primer Optional está vacío, prueba con otro Optional alternativo (aquí, los datos de
     *     cliente); al final `orElse` da un valor por defecto. Encadena alternativas sin `if/else`.
     */
    private static Tercero tercero(Usuario usuario) {
        String email = usuario.getEmail().valor();
        return usuario.getDatosProveedor()
                .map(datos -> new Tercero(usuario.getId(), datos.razonSocial(), email, "NIT", datos.nit(),
                        null, null))
                .or(() -> usuario.getDatosCliente()
                        .map(datos -> new Tercero(usuario.getId(), usuario.getNombre(), email, "C.C.",
                                datos.documento(), datos.telefono(), datos.direccion())))
                .orElse(new Tercero(usuario.getId(), usuario.getNombre(), email, null, null, null, null));
    }
}
