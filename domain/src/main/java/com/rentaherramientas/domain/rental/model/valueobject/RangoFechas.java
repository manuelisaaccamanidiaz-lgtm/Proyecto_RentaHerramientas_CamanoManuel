package com.rentaherramientas.domain.rental.model.valueobject;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;

/**
 * Periodo de alquiler con ambos extremos incluidos: una reserva del 1 al 1
 * dura un día. Garantiza fin >= inicio (ck_reservas_fechas).
 */
public record RangoFechas(LocalDate inicio, LocalDate fin) {

    public RangoFechas {
        if (inicio == null || fin == null) {
            throw new ValorInvalidoException("Las fechas de la reserva son obligatorias");
        }
        if (fin.isBefore(inicio)) {
            throw new ValorInvalidoException("La fecha de fin no puede ser anterior a la de inicio");
        }
    }

    /** Dos rangos se solapan si comparten al menos un día. */
    public boolean seSolapaCon(RangoFechas otro) {
        return !this.fin.isBefore(otro.inicio) && !otro.fin.isBefore(this.inicio);
    }

    public long dias() {
        return ChronoUnit.DAYS.between(inicio, fin) + 1;
    }
}
