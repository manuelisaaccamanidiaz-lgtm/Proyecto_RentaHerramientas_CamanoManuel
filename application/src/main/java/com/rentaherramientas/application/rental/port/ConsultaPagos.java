package com.rentaherramientas.application.rental.port;

import java.util.Collection;
import java.util.Set;

/**
 * Puerto de salida de rental hacia billing (anticorrupción): rental solo
 * necesita saber si una reserva está pagada, nunca los detalles del pago.
 */
public interface ConsultaPagos {

    boolean tienePagoAprobado(Long reservaId);

    /** De las reservas dadas, las que tienen un pago APROBADO (una sola consulta para toda una página). */
    Set<Long> reservasPagadas(Collection<Long> reservaIds);
}
