package com.rentaherramientas.domain.billing.exception;

import com.rentaherramientas.domain.shared.exception.DomainException;

/**
 * Regla: una reserva tiene como máximo un pago vigente (PENDIENTE o APROBADO);
 * solo tras un pago RECHAZADO se puede volver a pagar. Involucra varios pagos,
 * así que la comprueba el caso de uso y la garantiza el índice único parcial
 * uq_pagos_reserva_vigente ante pagos simultáneos.
 */
public class PagoDuplicadoException extends DomainException {

    public PagoDuplicadoException(Long reservaId) {
        super("La reserva " + reservaId + " ya tiene un pago pendiente o aprobado");
    }
}
