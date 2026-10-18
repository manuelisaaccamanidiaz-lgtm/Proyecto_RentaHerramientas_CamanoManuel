package com.rentaherramientas.application.shared.command;

import com.rentaherramientas.domain.identity.model.valueobject.Rol;
import com.rentaherramientas.domain.shared.model.Validaciones;

/**
 * Usuario autenticado que origina un command. El adaptador de entrada lo arma
 * con el claim "sub" (id) y el claim "rol" del token ya validado; nunca con
 * datos del cuerpo de la petición.
 */
public record Solicitante(Long id, Rol rol) {

    /** Constructor compacto: el id y el rol de quien hace la petición son obligatorios. */
    public Solicitante {
        Validaciones.requerido(id, "solicitante.id");
        Validaciones.requerido(rol, "solicitante.rol");
    }

    /** Indica si quien hace la petición es administrador (puede ver y gestionar todo). */
    public boolean esAdmin() {
        return rol == Rol.ADMIN;
    }
}
