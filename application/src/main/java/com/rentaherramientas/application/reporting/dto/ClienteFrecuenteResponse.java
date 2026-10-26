package com.rentaherramientas.application.reporting.dto;

import java.math.BigDecimal;

/**
 * Un cliente en el ranking de los más frecuentes: reservas que llegaron a
 * alquilarse y total pagado (pagos APROBADOS, en la moneda indicada).
 */
public record ClienteFrecuenteResponse(Long clienteId, String nombre, String email, long alquileres,
                                       String moneda, BigDecimal totalPagado) {
}
