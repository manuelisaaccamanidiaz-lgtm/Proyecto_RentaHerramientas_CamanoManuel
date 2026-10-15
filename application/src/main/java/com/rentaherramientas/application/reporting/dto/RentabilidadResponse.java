package com.rentaherramientas.application.reporting.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Ingresos (pagos APROBADOS) por herramienta y por proveedor, de mayor a menor.
 */
public record RentabilidadResponse(List<IngresoHerramienta> porHerramienta, List<IngresoProveedor> porProveedor) {

    public record IngresoHerramienta(Long herramientaId, String nombre, String proveedor, String moneda,
                                     BigDecimal ingresos, long pagos) {
    }

    public record IngresoProveedor(Long proveedorId, String razonSocial, String moneda, BigDecimal ingresos,
                                   long pagos) {
    }
}
