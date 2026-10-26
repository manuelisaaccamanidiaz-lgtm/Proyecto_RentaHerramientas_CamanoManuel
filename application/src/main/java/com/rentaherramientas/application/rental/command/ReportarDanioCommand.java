package com.rentaherramientas.application.rental.command;

import com.rentaherramientas.application.shared.command.Solicitante;

import java.math.BigDecimal;

/**
 * Reporte de daño tras la devolución. costoEstimado es opcional y se expresa en
 * la moneda de la reserva.
 */
public record ReportarDanioCommand(Long reservaId, Solicitante solicitante, String descripcion,
                                   BigDecimal costoEstimado) {
}
