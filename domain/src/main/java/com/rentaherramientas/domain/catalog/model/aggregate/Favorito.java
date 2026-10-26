package com.rentaherramientas.domain.catalog.model.aggregate;

import com.rentaherramientas.domain.shared.model.Validaciones;

import java.time.LocalDateTime;

/**
 * Marca de "favorita" que un cliente pone sobre una herramienta. Es su propio
 * agregado (y no una colección dentro de Herramienta) porque se crea y se borra
 * sin tocar la herramienta: meterla en Herramienta obligaría a cargar y guardar
 * todos sus favoritos cada vez que cambia su tarifa o su estado.
 * Su identidad es el par (clienteId, herramientaId); el cliente pertenece al BC
 * identity, por eso solo se guarda su id.
 */
public record Favorito(Long clienteId, Long herramientaId, LocalDateTime creadoEn) {

    /** Constructor compacto: los tres datos son obligatorios. */
    public Favorito {
        Validaciones.requerido(clienteId, "clienteId");
        Validaciones.requerido(herramientaId, "herramientaId");
        Validaciones.requerido(creadoEn, "creadoEn");
    }
}
