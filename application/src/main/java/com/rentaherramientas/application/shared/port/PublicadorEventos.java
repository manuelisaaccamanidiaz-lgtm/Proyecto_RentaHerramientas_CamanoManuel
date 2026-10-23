package com.rentaherramientas.application.shared.port;

import com.rentaherramientas.domain.shared.event.DomainEvent;

import java.util.List;

/**
 * Puerto de salida para avisar al resto del sistema de lo que ocurrió en los
 * agregados. Los casos de uso lo llaman DESPUÉS de guardar, con lo que devuelve
 * `agregado.extraerEventos()`, y DENTRO de su UnidadDeTrabajo: así quien escuche
 * puede esperar a que esa transacción se confirme (si se deshace, el evento no
 * llega). Cómo se reparten los eventos lo decide infraestructura.
 */
public interface PublicadorEventos {

    /** Publica los eventos en el orden recibido; una lista vacía no hace nada. */
    void publicar(List<DomainEvent> eventos);
}
