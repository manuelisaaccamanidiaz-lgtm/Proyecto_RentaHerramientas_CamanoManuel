package com.rentaherramientas.application.billing.port;

import com.rentaherramientas.domain.shared.model.valueobject.Dinero;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Vista de una reserva desde billing. estado es el nombre del estado en rental
 * (texto, para no depender de su enum); a billing solo le importa si está ACEPTADA.
 */
public record ReservaFacturable(Long reservaId, Long clienteId, Long proveedorId, String nombreHerramienta,
                                LocalDate fechaInicio, LocalDate fechaFin, long dias, Dinero precioTotal,
                                String estado) {

    public static final String ESTADO_ACEPTADA = "ACEPTADA";

    public boolean estaAceptada() {
        return ESTADO_ACEPTADA.equals(estado);
    }

    /**
     * Tarifa diaria pactada al reservar (precio total / días). Se deriva del precio
     * y no de la tarifa actual de la herramienta, que el proveedor pudo cambiar después.
     */
    public Dinero tarifaDiaria() {
        return Dinero.de(precioTotal.monto().divide(BigDecimal.valueOf(dias), 2, RoundingMode.HALF_UP),
                precioTotal.moneda());
    }
}
