package com.rentaherramientas.domain.shared.model.valueobject;

import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas del value object Dinero centradas en aplicarDescuento: el resultado
 * siempre queda en 2 decimales (HALF_UP) y el porcentaje solo admite 1..90.
 */
class DineroTest {

    private static Dinero cop(String monto) {
        return Dinero.de(new BigDecimal(monto), "COP");
    }

    @Test
    @DisplayName("45000.00 con 10 % = 40500.00")
    void descuentoExacto() {
        assertEquals(cop("40500.00"), cop("45000.00").aplicarDescuento(10));
    }

    @Test
    @DisplayName("33333.33 con 15 % = 28333.33 (28333.3305 se redondea a centavos sin fallar)")
    void descuentoConRedondeo() {
        Dinero resultado = cop("33333.33").aplicarDescuento(15);
        assertEquals(new BigDecimal("28333.33"), resultado.monto());
        assertEquals(2, resultado.monto().scale());
    }

    @Test
    @DisplayName("El redondeo es HALF_UP: 10000.05 con 10 % = 9000.045 → 9000.05")
    void redondeoHalfUp() {
        assertEquals(cop("9000.05"), cop("10000.05").aplicarDescuento(10));
    }

    @Test
    @DisplayName("Conserva la moneda")
    void conservaMoneda() {
        assertEquals("USD", Dinero.de(new BigDecimal("100"), "USD").aplicarDescuento(50).moneda());
    }

    @Test
    @DisplayName("Los extremos 1 y 90 se aceptan")
    void extremos() {
        assertEquals(cop("99.00"), cop("100").aplicarDescuento(1));
        assertEquals(cop("10.00"), cop("100").aplicarDescuento(90));
    }

    @ParameterizedTest(name = "rechaza {0} %")
    @ValueSource(ints = {0, 91, -5, 100})
    @DisplayName("Un porcentaje fuera de 1..90 lanza ValorInvalidoException")
    void fueraDeRango(int porcentaje) {
        Dinero tarifa = cop("45000.00");
        assertThrows(ValorInvalidoException.class, () -> tarifa.aplicarDescuento(porcentaje));
    }
}
