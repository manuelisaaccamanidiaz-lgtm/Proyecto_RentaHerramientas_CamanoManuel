package com.rentaherramientas.infrastructure.catalog.adapters.in.rest;

import com.rentaherramientas.application.catalog.command.ActualizarHerramientaCommand;
import com.rentaherramientas.application.catalog.command.RegistrarHerramientaCommand;
import com.rentaherramientas.application.shared.command.Solicitante;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Cuerpo de POST /api/herramientas y PUT /api/herramientas/{id}. No tiene
 * proveedorId: el proveedor sale siempre del JWT. moneda es opcional (COP).
 */
public record HerramientaRequest(
        @NotBlank @Size(max = 120) String nombre,
        @Size(max = 2000) String descripcion,
        @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal tarifaDiaria,
        @Pattern(regexp = "^[A-Z]{3}$", message = "debe ser un código ISO 4217 de 3 letras mayúsculas") String moneda,
        @NotEmpty Set<@NotNull Long> categoriaIds) {

    public RegistrarHerramientaCommand toRegistrarCommand(Solicitante solicitante) {
        return new RegistrarHerramientaCommand(solicitante, nombre, descripcion, tarifaDiaria, moneda, categoriaIds);
    }

    public ActualizarHerramientaCommand toActualizarCommand(Long herramientaId, Solicitante solicitante) {
        return new ActualizarHerramientaCommand(herramientaId, solicitante, nombre, descripcion, tarifaDiaria,
                moneda, categoriaIds);
    }
}
