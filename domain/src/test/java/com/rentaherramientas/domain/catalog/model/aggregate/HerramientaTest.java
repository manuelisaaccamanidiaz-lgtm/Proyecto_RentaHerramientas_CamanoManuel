package com.rentaherramientas.domain.catalog.model.aggregate;

import com.rentaherramientas.domain.catalog.model.valueobject.EstadoHerramienta;
import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;
import com.rentaherramientas.domain.shared.model.valueobject.Dinero;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas del descuento de Herramienta: la tarifa que paga una reserva nueva
 * (tarifaConDescuento) y la validación 1..90 o null en todos los caminos de entrada.
 */
class HerramientaTest {

    private static final Dinero TARIFA = Dinero.de(new BigDecimal("45000.00"), "COP");

    private static Herramienta registrar(Integer descuento) {
        return Herramienta.registrar(2L, "Taladro", null, "Bosch", TARIFA, descuento, Set.of(1L));
    }

    @Test
    @DisplayName("Sin descuento, tarifaConDescuento == tarifaDiaria")
    void sinDescuento() {
        Herramienta herramienta = registrar(null);
        assertNull(herramienta.getDescuentoPorcentaje());
        assertEquals(herramienta.getTarifaDiaria(), herramienta.tarifaConDescuento());
    }

    @Test
    @DisplayName("Con 10 %, la tarifa con descuento es 40500.00 y la tarifa diaria no cambia")
    void conDescuento() {
        Herramienta herramienta = registrar(10);
        assertEquals(Dinero.de(new BigDecimal("40500.00"), "COP"), herramienta.tarifaConDescuento());
        assertEquals(TARIFA, herramienta.getTarifaDiaria());
    }

    @Test
    @DisplayName("definirDescuento lo cambia y con null lo quita")
    void definirYQuitar() {
        Herramienta herramienta = registrar(null);
        herramienta.definirDescuento(20);
        assertEquals(new BigDecimal("36000.00"), herramienta.tarifaConDescuento().monto());

        herramienta.definirDescuento(null);
        assertEquals(TARIFA, herramienta.tarifaConDescuento());
    }

    @ParameterizedTest(name = "registrar rechaza {0} %")
    @ValueSource(ints = {0, 91, -1})
    @DisplayName("Un descuento fuera de rango al registrar lanza ValorInvalidoException")
    void registrarFueraDeRango(int descuento) {
        assertThrows(ValorInvalidoException.class, () -> registrar(descuento));
    }

    @ParameterizedTest(name = "definirDescuento rechaza {0} %")
    @ValueSource(ints = {0, 91})
    @DisplayName("Un descuento fuera de rango al actualizar lanza excepción y no cambia el actual")
    void definirFueraDeRango(int descuento) {
        Herramienta herramienta = registrar(10);
        assertThrows(ValorInvalidoException.class, () -> herramienta.definirDescuento(descuento));
        assertEquals(10, herramienta.getDescuentoPorcentaje());
    }

    @Test
    @DisplayName("reconstruir también valida el rango (dato corrupto en BD)")
    void reconstruirValida() {
        assertThrows(ValorInvalidoException.class, () -> Herramienta.reconstruir(1L, 2L, "Taladro", null, null,
                TARIFA, 95, EstadoHerramienta.DISPONIBLE, List.of(), Set.of(1L)));
    }
}
