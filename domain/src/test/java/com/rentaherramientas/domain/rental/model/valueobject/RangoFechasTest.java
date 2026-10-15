package com.rentaherramientas.domain.rental.model.valueobject;

import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del value object RangoFechas: solapamiento con extremos incluidos (la
 * misma regla que aplica la restricción EXCLUDE de la base) y conteo de días.
 */
class RangoFechasTest {

    private static RangoFechas rango(int diaInicio, int diaFin) {
        return new RangoFechas(LocalDate.of(2026, 11, diaInicio), LocalDate.of(2026, 11, diaFin));
    }

    @Test
    @DisplayName("Cruce parcial: se solapan, en ambos sentidos")
    void cruceParcialSeSolapa() {
        assertTrue(rango(1, 5).seSolapaCon(rango(4, 8)));
        assertTrue(rango(4, 8).seSolapaCon(rango(1, 5)));
    }

    @Test
    @DisplayName("Sin cruce: rangos separados no se solapan, en ambos sentidos")
    void sinCruceNoSeSolapa() {
        assertFalse(rango(1, 3).seSolapaCon(rango(5, 8)));
        assertFalse(rango(5, 8).seSolapaCon(rango(1, 3)));
    }

    @Test
    @DisplayName("Borde: terminar y empezar el mismo día SÍ es solapamiento (extremos incluidos)")
    void bordeDelMismoDiaSeSolapa() {
        assertTrue(rango(1, 5).seSolapaCon(rango(5, 8)));
        assertTrue(rango(5, 8).seSolapaCon(rango(1, 5)));
    }

    @Test
    @DisplayName("Borde: días consecutivos sin compartir ninguno no se solapan")
    void diasConsecutivosNoSeSolapan() {
        assertFalse(rango(1, 4).seSolapaCon(rango(5, 8)));
    }

    @Test
    @DisplayName("Uno contenido en otro: se solapan, en ambos sentidos")
    void contenidoSeSolapa() {
        assertTrue(rango(1, 10).seSolapaCon(rango(3, 4)));
        assertTrue(rango(3, 4).seSolapaCon(rango(1, 10)));
    }

    @Test
    @DisplayName("Un rango se solapa consigo mismo")
    void mismoRangoSeSolapa() {
        assertTrue(rango(2, 6).seSolapaCon(rango(2, 6)));
    }

    @Test
    @DisplayName("dias() cuenta ambos extremos")
    void diasIncluyeAmbosExtremos() {
        assertEquals(1, rango(7, 7).dias());
        assertEquals(5, rango(1, 5).dias());
        assertEquals(3, new RangoFechas(LocalDate.of(2026, 12, 31), LocalDate.of(2027, 1, 2)).dias());
    }

    @Test
    @DisplayName("Fin anterior al inicio o fechas nulas: ValorInvalidoException")
    void rangoInvalido() {
        assertThrows(ValorInvalidoException.class, () -> rango(5, 4));
        assertThrows(ValorInvalidoException.class, () -> new RangoFechas(null, LocalDate.of(2026, 11, 1)));
        assertThrows(ValorInvalidoException.class, () -> new RangoFechas(LocalDate.of(2026, 11, 1), null));
    }
}
