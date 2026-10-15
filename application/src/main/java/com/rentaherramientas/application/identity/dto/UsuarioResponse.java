package com.rentaherramientas.application.identity.dto;

import com.rentaherramientas.domain.identity.model.aggregate.Usuario;
import com.rentaherramientas.domain.identity.model.valueobject.Rol;

/**
 * Vista pública de un usuario. Nunca incluye el hash de la contraseña.
 */
public record UsuarioResponse(Long id, String nombre, String email, Rol rol, boolean activo) {

    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail().valor(),
                usuario.getRol(),
                usuario.isActivo());
    }
}
