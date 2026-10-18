package com.rentaherramientas.infrastructure.catalog.adapters.in.rest;

import com.rentaherramientas.application.catalog.command.GuardarCategoriaCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de POST /api/categorias y PUT /api/categorias/{id}.
 */
public record CategoriaRequest(
        @NotBlank @Size(max = 80) String nombre,
        @Size(max = 1000) String descripcion) {

    /** Convierte el cuerpo HTTP en el command de aplicación. */
    public GuardarCategoriaCommand toCommand() {
        return new GuardarCategoriaCommand(nombre, descripcion);
    }
}
