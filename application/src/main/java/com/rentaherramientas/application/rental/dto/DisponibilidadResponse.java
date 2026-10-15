package com.rentaherramientas.application.rental.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Periodos ocupados de una herramienta dentro de [desde, hasta], para que el
 * selector de fechas del frontend los bloquee. Es público: no revela quién
 * reservó ni en qué estado está cada reserva.
 */
public record DisponibilidadResponse(Long herramientaId, LocalDate desde, LocalDate hasta,
                                     List<RangoOcupado> ocupados) {

    /** Ambas fechas incluidas. */
    public record RangoOcupado(LocalDate inicio, LocalDate fin) {
    }
}
