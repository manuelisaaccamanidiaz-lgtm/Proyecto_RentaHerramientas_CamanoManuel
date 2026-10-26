package com.rentaherramientas.domain.billing.model.valueobject;

/**
 * Ciclo de vida de un pago: nace PENDIENTE y termina en un único estado final
 * (APROBADO o RECHAZADO). Replicado en ck_pagos_estado.
 */
public enum EstadoPago {
    PENDIENTE,
    APROBADO,
    RECHAZADO;

    /** Indica si el estado es definitivo (APROBADO o RECHAZADO), es decir, distinto de PENDIENTE. */
    public boolean esFinal() {
        return this != PENDIENTE;
    }
}
