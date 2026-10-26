package com.rentaherramientas.application.reporting.dto;

import java.math.BigDecimal;

public record HerramientaDaniosResponse(Long herramientaId, String nombre, String proveedor,
                                        Long reportes, BigDecimal costoTotal) {

}
