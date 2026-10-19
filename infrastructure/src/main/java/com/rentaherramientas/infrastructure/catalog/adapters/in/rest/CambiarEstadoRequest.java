package com.rentaherramientas.infrastructure.catalog.adapters.in.rest;

import com.rentaherramientas.domain.catalog.model.valueobject.EstadoHerramienta;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo de PATCH /api/herramientas/{id}/estado.
 */
public record CambiarEstadoRequest(
        @Schema(description = "Nuevo estado de la herramienta", example = "MANTENIMIENTO")
        @NotNull EstadoHerramienta estado) {
}
