package com.rentaherramientas.application.billing.usecase;

import com.rentaherramientas.application.billing.exception.ReservaNoEncontradaException;
import com.rentaherramientas.application.billing.port.ConsultaReservas;
import com.rentaherramientas.application.billing.port.ReservaFacturable;
import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.application.shared.exception.AccesoDenegadoException;
import com.rentaherramientas.domain.identity.model.valueobject.Rol;

/**
 * Reglas de acceso de billing, compartidas por sus casos de uso. Pagos y
 * facturas pertenecen a una reserva: los VE su cliente, el proveedor de la
 * herramienta o un ADMIN; los GESTIONA (confirmar o rechazar un pago en
 * efectivo) el proveedor o un ADMIN.
 */
final class AccesoFacturacion {

    private AccesoFacturacion() {
    }

    static ReservaFacturable cargarReserva(ConsultaReservas consultaReservas, Long reservaId) {
        return consultaReservas.buscar(reservaId).orElseThrow(() -> new ReservaNoEncontradaException(reservaId));
    }

    static void exigirVer(ReservaFacturable reserva, Solicitante solicitante) {
        boolean esSuCliente = solicitante.rol() == Rol.CLIENTE && reserva.clienteId().equals(solicitante.id());
        if (!solicitante.esAdmin() && !esSuCliente && !esSuProveedor(reserva, solicitante)) {
            throw new AccesoDenegadoException("No tienes acceso a los pagos ni facturas de esta reserva");
        }
    }

    static void exigirGestionar(ReservaFacturable reserva, Solicitante solicitante) {
        if (!solicitante.esAdmin() && !esSuProveedor(reserva, solicitante)) {
            throw new AccesoDenegadoException("Solo el proveedor de la herramienta o un ADMIN gestionan este pago");
        }
    }

    private static boolean esSuProveedor(ReservaFacturable reserva, Solicitante solicitante) {
        return solicitante.rol() == Rol.PROVEEDOR && reserva.proveedorId().equals(solicitante.id());
    }
}
