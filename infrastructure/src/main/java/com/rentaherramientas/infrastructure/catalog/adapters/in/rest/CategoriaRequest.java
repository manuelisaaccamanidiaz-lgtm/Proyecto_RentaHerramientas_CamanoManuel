package com.rentaherramientas.infrastructure.catalog.adapters.in.rest;

import com.rentaherramientas.application.catalog.command.GuardarCategoriaCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de POST /api/categorias y PUT /api/categorias/{id}.
 */
public record CategoriaRequest(
        @Schema(description = "Nombre único de la categoría", example = "Herramientas eléctricas")
        @NotBlank @Size(max = 80) String nombre,
        @Schema(description = "Descripción opcional de la categoría",
                example = "Taladros, pulidoras y sierras para obra y remodelación en Bucaramanga")
        @Size(max = 1000) String descripcion) {

    /** Convierte el cuerpo HTTP en el command de aplicación. */
    public GuardarCategoriaCommand toCommand() {
        return new GuardarCategoriaCommand(nombre, descripcion);
    }
}
