package com.rentaherramientas.application.rental.usecase;

import com.rentaherramientas.domain.rental.model.valueobject.RangoFechas;
import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Reglas de fechas de una reserva, compartidas por crear y reprogramar: no puede
 * empezar antes de hoy (según el reloj de negocio) ni durar más de
 * {@value #DURACION_MAXIMA_DIAS} días. Ambas responden 400.
 */
final class ReglasFechasReserva {

    static final int DURACION_MAXIMA_DIAS = 30;

    /** Constructor privado: clase de utilidades estáticas. */
    private ReglasFechasReserva() {
    }

    /** Lanza ValorInvalidoException si el rango empieza en el pasado o supera la duración máxima. */
    static void validar(RangoFechas rango, Clock clock) {
        if (rango.inicio().isBefore(LocalDate.now(clock))) {
            throw new ValorInvalidoException("La fecha de inicio no puede ser anterior a hoy");
        }
        if (rango.dias() > DURACION_MAXIMA_DIAS) {
            throw new ValorInvalidoException("Una reserva puede durar como máximo " + DURACION_MAXIMA_DIAS + " días");
        }
    }
}
