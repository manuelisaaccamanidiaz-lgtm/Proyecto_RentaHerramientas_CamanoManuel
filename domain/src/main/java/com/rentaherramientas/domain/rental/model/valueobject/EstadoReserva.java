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
    VENCIDA;

    public boolean puedeCambiarA(EstadoReserva destino) {
        return switch (this) {
            case PENDIENTE -> destino == ACEPTADA || destino == RECHAZADA || destino == VENCIDA;
            case ACEPTADA -> destino == ENTREGADA || destino == VENCIDA;
            case ENTREGADA -> destino == DEVUELTA;
            case RECHAZADA, DEVUELTA, VENCIDA -> false;
        };
    }

    /** Estados que ocupan la herramienta en su rango de fechas e impiden otra reserva. */
    public boolean bloqueaDisponibilidad() {
        return this == PENDIENTE || this == ACEPTADA || this == ENTREGADA;
    }
}
