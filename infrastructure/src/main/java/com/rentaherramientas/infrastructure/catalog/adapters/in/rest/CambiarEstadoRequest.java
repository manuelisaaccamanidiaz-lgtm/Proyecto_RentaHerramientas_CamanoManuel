package com.rentaherramientas.infrastructure.catalog.adapters.in.rest;

import com.rentaherramientas.domain.catalog.model.valueobject.EstadoHerramienta;
import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo de PATCH /api/herramientas/{id}/estado.
 */
public record CambiarEstadoRequest(@NotNull EstadoHerramienta estado) {
}
