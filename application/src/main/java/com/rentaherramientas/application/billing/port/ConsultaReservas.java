package com.rentaherramientas.application.billing.port;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida de billing hacia rental y catalog (capa anticorrupción):
 * billing ve la reserva con el modelo que necesita para cobrar y facturar, y
 * nunca importa clases de esos BC. El adaptador vive en infraestructura.
 */
public interface ConsultaReservas {

    Optional<ReservaFacturable> buscar(Long reservaId);

    List<Long> reservaIdsDelCliente(Long clienteId);

    /** Reservas de todas las herramientas del proveedor. */
    List<Long> reservaIdsDelProveedor(Long proveedorId);
}
