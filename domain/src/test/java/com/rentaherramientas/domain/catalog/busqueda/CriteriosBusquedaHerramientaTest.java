package com.rentaherramientas.domain.catalog.busqueda;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.rentaherramientas.domain.catalog.port.repository.CriteriosBusquedaHerramienta;
import com.rentaherramientas.domain.shared.model.SolicitudPagina;

class CriteriosBusquedaHerramientaTest {

    @Test
    @DisplayName("Un rango válido no lanza excepción")
    void rangoValido() {
        assertDoesNotThrow(() -> new CriteriosBusquedaHerramienta(
            null, null, null, null, null, new BigDecimal("10000"), new BigDecimal("50000"),
            true, new SolicitudPagina(0, 10)));
    }
}
