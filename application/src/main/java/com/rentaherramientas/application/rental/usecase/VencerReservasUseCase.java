package com.rentaherramientas.application.rental.usecase;

/**
 * Pasa a VENCIDA las reservas que ya no pueden cumplirse; lo ejecuta una tarea programada.
 */
public interface VencerReservasUseCase {

    /** Devuelve cuántas reservas venció. */
    int vencer();
}
