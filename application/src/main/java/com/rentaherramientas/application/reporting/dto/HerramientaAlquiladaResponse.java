package com.rentaherramientas.application.reporting.dto;

/**
 * Una herramienta en el ranking de las más alquiladas: cuántas veces salió
 * (reservas ENTREGADAS o DEVUELTAS) y cuántos días en total.
 */
public record HerramientaAlquiladaResponse(Long herramientaId, String nombre, String proveedor,
                                           long alquileres, long diasAlquilada) {
}
