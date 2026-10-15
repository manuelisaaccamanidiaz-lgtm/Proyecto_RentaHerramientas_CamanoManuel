package com.rentaherramientas.application.shared.command;

import com.rentaherramientas.domain.identity.model.valueobject.Rol;
import com.rentaherramientas.domain.shared.model.Validaciones;

/**
 * Usuario autenticado que origina un command. El adaptador de entrada lo arma
 * con el claim "sub" (id) y el claim "rol" del token ya validado; nunca con
 * datos del cuerpo de la petición.
 */
public record Solicitante(Long id, Rol rol) {

    public Solicitante {
        Validaciones.requerido(id, "solicitante.id");
        Validaciones.requerido(rol, "solicitante.rol");
    }

    public boolean esAdmin() {
        return rol == Rol.ADMIN;
    }
}
