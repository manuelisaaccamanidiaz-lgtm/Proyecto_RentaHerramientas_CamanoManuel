package com.rentaherramientas.domain.billing.model.valueobject;

/**
 * Medios de pago aceptados. Conjunto cerrado replicado en ck_pagos_metodo.
 */
public enum MetodoPago {
    TARJETA,
    PSE,
    EFECTIVO;

    /**
     * Pagos simulados: TARJETA y PSE se aprueban en el momento (como lo haría la
     * pasarela); EFECTIVO queda PENDIENTE hasta que el proveedor confirme que lo recibió.
     */
    public boolean seApruebaAlInstante() {
        return this != EFECTIVO;
    }
}
