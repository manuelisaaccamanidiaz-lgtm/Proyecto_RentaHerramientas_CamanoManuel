package com.rentaherramientas.application.reporting.dto;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Los mismos indicadores del resumen general, limitados a las herramientas de un proveedor.
 */
public record ResumenProveedorResponse(Long proveedorId,
                                       Map<String, BigDecimal> ingresosPorMoneda,
                                       long pagosAprobados,
                                       Map<String, Long> reservasPorEstado,
                                       Map<String, Long> herramientasPorEstado) {
}
