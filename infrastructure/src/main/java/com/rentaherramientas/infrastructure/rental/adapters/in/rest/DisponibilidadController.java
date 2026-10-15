package com.rentaherramientas.infrastructure.rental.adapters.in.rest;

import com.rentaherramientas.application.rental.dto.DisponibilidadResponse;
import com.rentaherramientas.application.rental.usecase.ConsultarReservasUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Adaptador de entrada público para el selector de fechas del frontend. Cuelga
 * de /api/herramientas pero pertenece a rental: las reservas son quienes ocupan fechas.
 */
@RestController
@Tag(name = "Reservas", description = "Reservas de herramientas y su ciclo de vida")
public class DisponibilidadController {

    private final ConsultarReservasUseCase consultarReservas;

    public DisponibilidadController(ConsultarReservasUseCase consultarReservas) {
        this.consultarReservas = consultarReservas;
    }

    @GetMapping("/api/herramientas/{id}/disponibilidad")
    @SecurityRequirements
    @Operation(summary = "Rangos de fechas ocupados de una herramienta (público)",
            description = "Por defecto desde hoy y 90 días; periodo máximo 366 días.")
    public DisponibilidadResponse disponibilidad(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return consultarReservas.disponibilidad(id, desde, hasta);
    }
}
