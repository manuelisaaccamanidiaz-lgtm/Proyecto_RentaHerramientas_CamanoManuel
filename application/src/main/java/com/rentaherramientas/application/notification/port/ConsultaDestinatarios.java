package com.rentaherramientas.application.notification.port;

import java.util.Optional;

/**
 * Puerto anticorrupción de notification: los eventos solo traen ids, y para
 * redactar el aviso hace falta saber a quién va (proveedor, cliente) y el nombre
 * de la herramienta. Lo implementa infraestructura con los repositorios de
 * catalog, rental y billing.
 */
public interface ConsultaDestinatarios {

    /** Nombre y proveedor de la herramienta; vacío si no existe. */
    Optional<HerramientaNotificable> herramienta(Long herramientaId);

    /** Cliente, proveedor y herramienta de la reserva; vacío si no existe. */
    Optional<ReservaNotificable> reserva(Long reservaId);

    /** Número de la factura del pago aprobado de la reserva; vacío si aún no hay factura. */
    Optional<String> numeroFacturaDeReserva(Long reservaId);
}
