package com.rentaherramientas.domain.catalog.model.valueobject;

/**
 * Estado operativo de una unidad física de herramienta. Encapsula la máquina de
 * estados para que ninguna parte del sistema pueda saltarse una transición
 * (p. ej. pasar de DANADA a DISPONIBLE sin pasar por mantenimiento).
 */
public enum EstadoHerramienta {
    DISPONIBLE,
    EN_MANTENIMIENTO,
    DANADA,
    INACTIVA;

    /**
     * Indica si se permite pasar del estado actual al estado destino.
     *
     * TÉCNICA AVANZADA (Java): máquina de estados dentro del enum
     *   Qué hace: las transiciones válidas viven en el propio enum con un `switch (this)`, de modo que la
     *     regla está en un solo lugar y el compilador obliga a cubrir cada estado.
     *   Cómo usarlo: `if (!estado.puedeCambiarA(nuevo)) throw ...;`
     */
    public boolean puedeCambiarA(EstadoHerramienta destino) {
        return switch (this) {
            case DISPONIBLE -> destino == EN_MANTENIMIENTO || destino == DANADA || destino == INACTIVA;
            case EN_MANTENIMIENTO -> destino == DISPONIBLE || destino == DANADA || destino == INACTIVA;
            case DANADA -> destino == EN_MANTENIMIENTO || destino == INACTIVA;
            case INACTIVA -> destino == DISPONIBLE;
        };
    }
}
