package com.rentaherramientas.application.rental.command;

import com.rentaherramientas.domain.rental.model.valueobject.EstadoReserva;

import java.time.LocalDate;

/**
 * Filtros opcionales (null = sin filtrar) del historial de reservas del ADMIN.
 * desde/hasta devuelven las reservas cuyo periodo se cruza con ese intervalo.
 */
public record FiltroHistorialReservas(EstadoReserva estado, Long clienteId, Long herramientaId,
                                      LocalDate desde, LocalDate hasta, int pagina, int tamano) {
}
