package com.rentaherramientas.domain.rental.model.valueobject;

/**
 * Ciclo de vida de una reserva. Encapsula la máquina de estados para que nadie
 * pueda, por ejemplo, entregar una reserva que nunca fue aceptada.
 * RECHAZADA, DEVUELTA y VENCIDA son estados finales.
 */
public enum EstadoReserva {
    PENDIENTE,
    ACEPTADA,
    RECHAZADA,
    ENTREGADA,
    DEVUELTA,
    CANCELADA,
    VENCIDA;

    /**
     * Indica si se permite pasar del estado actual al estado destino.
     *
     * TÉCNICA AVANZADA (Java): varias constantes en un mismo `case`
     *   Qué hace: `case RECHAZADA, DEVUELTA, VENCIDA -> false;` agrupa los estados finales, de los que ya no
     *     se puede salir.
     */
    public boolean puedeCambiarA(EstadoReserva destino) {
        return switch (this) {
            case PENDIENTE -> destino == ACEPTADA || destino == RECHAZADA || destino == VENCIDA || destino == CANCELADA;
            case ACEPTADA -> destino == ENTREGADA || destino == VENCIDA;
            case ENTREGADA -> destino == DEVUELTA;
            case RECHAZADA, DEVUELTA, VENCIDA , CANCELADA-> false;
        };
    }

    /** Estados que ocupan la herramienta en su rango de fechas e impiden otra reserva. */
    public boolean bloqueaDisponibilidad() {
        return this == PENDIENTE || this == ACEPTADA || this == ENTREGADA;
    }
}
