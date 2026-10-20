package com.rentaherramientas.application.catalog.command;

import com.rentaherramientas.application.shared.command.Solicitante;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Reemplaza datos, tarifa y categorías de una herramienta. categoriaIds es el
 * conjunto final: las que no estén se quitan y las nuevas se asignan.
 * moneda null = moneda por defecto (COP).
 */
public record ActualizarHerramientaCommand(Long herramientaId, Solicitante solicitante, String nombre,
                                           String descripcion, String marca, BigDecimal tarifaDiaria, String moneda,
                                           Set<Long> categoriaIds) {
}
