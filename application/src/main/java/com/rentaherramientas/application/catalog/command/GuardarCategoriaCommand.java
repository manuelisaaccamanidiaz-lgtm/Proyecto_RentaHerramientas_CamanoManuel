package com.rentaherramientas.application.catalog.command;

/**
 * Datos de una categoría para crearla o actualizarla (descripción opcional).
 */
public record GuardarCategoriaCommand(String nombre, String descripcion) {
}
