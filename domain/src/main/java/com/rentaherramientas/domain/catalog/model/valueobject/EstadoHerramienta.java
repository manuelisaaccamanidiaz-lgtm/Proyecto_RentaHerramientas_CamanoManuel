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

    public boolean puedeCambiarA(EstadoHerramienta destino) {
        return switch (this) {
            case DISPONIBLE -> destino == EN_MANTENIMIENTO || destino == DANADA || destino == INACTIVA;
            case EN_MANTENIMIENTO -> destino == DISPONIBLE || destino == DANADA || destino == INACTIVA;
            case DANADA -> destino == EN_MANTENIMIENTO || destino == INACTIVA;
            case INACTIVA -> destino == DISPONIBLE;
        };
    }
}
