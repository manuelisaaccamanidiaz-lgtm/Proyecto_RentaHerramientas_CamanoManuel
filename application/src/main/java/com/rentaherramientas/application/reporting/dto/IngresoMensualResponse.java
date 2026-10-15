package com.rentaherramientas.application.reporting.dto;

import java.math.BigDecimal;

/**
 * Ingresos (pagos APROBADOS) de un mes; mes en formato yyyy-MM.
 */
public record IngresoMensualResponse(String mes, String moneda, BigDecimal total, long pagos) {
}
