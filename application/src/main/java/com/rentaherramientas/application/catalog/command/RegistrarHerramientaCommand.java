package com.rentaherramientas.application.catalog.command;

import com.rentaherramientas.application.shared.command.Solicitante;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Intención de dar de alta una herramienta. El proveedor es siempre el
 * solicitante autenticado: el cuerpo de la petición no puede elegirlo.
 * moneda null = moneda por defecto (COP); descuentoPorcentaje null = sin descuento.
 */
public record RegistrarHerramientaCommand(Solicitante solicitante, String nombre, String descripcion, String marca,
                                          BigDecimal tarifaDiaria, String moneda, Integer descuentoPorcentaje,
                                          Set<Long> categoriaIds) {
}
