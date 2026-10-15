package com.rentaherramientas.infrastructure.identity.adapters.out.persistence;

import com.rentaherramientas.domain.identity.model.aggregate.Usuario;
import com.rentaherramientas.domain.identity.model.valueobject.DatosCliente;
import com.rentaherramientas.domain.identity.model.valueobject.DatosProveedor;
import com.rentaherramientas.domain.identity.model.valueobject.Email;
import com.rentaherramientas.domain.identity.model.valueobject.PasswordHash;
import org.springframework.stereotype.Component;

/**
 * Traduce entre el agregado Usuario y su modelo de persistencia. Aplana los VO
 * (Email, PasswordHash, DatosProveedor, DatosCliente) en columnas y los
 * reconstruye al leer, de modo que las validaciones del dominio se vuelven a
 * aplicar en cada lectura.
 */
@Component
public class UsuarioPersistenceMapper {

    public Usuario toDomain(UsuarioEntity entity) {
        ProveedorEntity proveedor = entity.getProveedor();
        DatosProveedor datosProveedor = proveedor == null
                ? null
                : new DatosProveedor(proveedor.getRazonSocial(), proveedor.getNit());
        ClienteEntity cliente = entity.getCliente();
        DatosCliente datosCliente = cliente == null
                ? null
                : new DatosCliente(cliente.getDocumento(), cliente.getTelefono(), cliente.getDireccion());
        return Usuario.reconstruir(
                entity.getId(),
                entity.getNombre(),
                new Email(entity.getEmail()),
                new PasswordHash(entity.getPasswordHash()),
                entity.getRol(),
                entity.isActivo(),
                datosProveedor,
                datosCliente);
    }

    public UsuarioEntity toEntity(Usuario usuario) {
        UsuarioEntity entity = new UsuarioEntity(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail().valor(),
                usuario.getPasswordHash().valor(),
                usuario.getRol(),
                usuario.isActivo());
        usuario.getDatosProveedor().ifPresent(datos -> entity.asignarProveedor(
                new ProveedorEntity(usuario.getId(), datos.razonSocial(), datos.nit())));
        usuario.getDatosCliente().ifPresent(datos -> entity.asignarCliente(
                new ClienteEntity(usuario.getId(), datos.documento(), datos.telefono(), datos.direccion())));
        return entity;
    }
}
