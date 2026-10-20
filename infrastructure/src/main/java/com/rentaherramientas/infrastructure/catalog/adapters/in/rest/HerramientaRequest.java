package com.rentaherramientas.infrastructure.catalog.adapters.in.rest;

import com.rentaherramientas.application.catalog.command.ActualizarHerramientaCommand;
import com.rentaherramientas.application.catalog.command.RegistrarHerramientaCommand;
import com.rentaherramientas.application.shared.command.Solicitante;
import io.swagger.v3.oas.annotations.media.Schema;
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
 *
 * TÉCNICA AVANZADA (Bean Validation): anotaciones sobre tipos genéricos
 *   Qué hace: `Set<@NotNull Long>` valida cada elemento del conjunto, no solo el conjunto.
 *     `@Digits(integer = 10, fraction = 2)` limita la tarifa a NUMERIC(12,2).
 */
public record HerramientaRequest(
        @Schema(description = "Nombre comercial de la herramienta", example = "Taladro percutor DeWalt 800 W")
        @NotBlank @Size(max = 120) String nombre,
        @Schema(description = "Descripción opcional (estado, accesorios incluidos...)",
                example = "Incluye maletín, 2 brocas de concreto y extensión. Entrega en Cabecera del Llano.")
        @Size(max = 2000) String descripcion,
        @Size(max = 60)
        @Schema(description = "marca del fabricante(opcional)",example = "Bosch")String marca,
        @Schema(description = "Tarifa por día en la moneda indicada", example = "45000.00")
        @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal tarifaDiaria,
        @Schema(description = "Código ISO 4217; si se omite se usa COP", example = "COP")
        @Pattern(regexp = "^[A-Z]{3}$", message = "debe ser un código ISO 4217 de 3 letras mayúsculas") String moneda,
        @Schema(description = "Ids de las categorías a las que pertenece (al menos una)", example = "[1, 3]")
        @NotEmpty Set<@NotNull Long> categoriaIds) {

    /** Convierte el cuerpo HTTP en el command de alta, con el proveedor tomado del JWT. */
    public RegistrarHerramientaCommand toRegistrarCommand(Solicitante solicitante) {
        return new RegistrarHerramientaCommand(solicitante, nombre, descripcion, marca, tarifaDiaria, moneda, categoriaIds);
    }

    /** Convierte el cuerpo HTTP en el command de actualización de la herramienta indicada. */
    public ActualizarHerramientaCommand toActualizarCommand(Long herramientaId, Solicitante solicitante) {
        return new ActualizarHerramientaCommand(herramientaId, solicitante, nombre, descripcion, marca, tarifaDiaria,
                moneda, categoriaIds);
    }
}
