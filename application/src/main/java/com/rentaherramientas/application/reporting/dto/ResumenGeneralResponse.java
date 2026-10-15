package com.rentaherramientas.application.reporting.dto;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Indicadores globales de la plataforma. Los ingresos son la suma de los pagos
 * APROBADOS, agrupada por moneda (no se suman monedas distintas).
 */
public record ResumenGeneralResponse(Map<String, BigDecimal> ingresosPorMoneda,
                                     long pagosAprobados,
                                     long facturasEmitidas,
                                     Map<String, Long> reservasPorEstado,
                                     Map<String, Long> usuariosPorRol,
                                     Map<String, Long> herramientasPorEstado) {
}
