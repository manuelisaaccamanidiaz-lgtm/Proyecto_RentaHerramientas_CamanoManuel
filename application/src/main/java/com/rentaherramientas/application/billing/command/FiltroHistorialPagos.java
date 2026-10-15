package com.rentaherramientas.application.billing.command;

import com.rentaherramientas.domain.billing.model.valueobject.EstadoPago;
import com.rentaherramientas.domain.billing.model.valueobject.MetodoPago;

import java.time.LocalDate;

/**
 * Filtros opcionales (null = sin filtrar) del historial de pagos del ADMIN;
 * desde/hasta filtran por la fecha del pago, ambos días incluidos.
 */
public record FiltroHistorialPagos(EstadoPago estado, MetodoPago metodo, LocalDate desde, LocalDate hasta,
                                   int pagina, int tamano) {
}
