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

    /** Constructor privado: clase de utilidades estáticas. */
    private AccesoFacturacion() {
    }

    /** Carga la reserva o lanza ReservaNoEncontradaException (404). */
    static ReservaFacturable cargarReserva(ConsultaReservas consultaReservas, Long reservaId) {
        return consultaReservas.buscar(reservaId).orElseThrow(() -> new ReservaNoEncontradaException(reservaId));
    }

    /** Permite ver pagos y facturas al cliente de la reserva, al proveedor de la herramienta o a un ADMIN. */
    static void exigirVer(ReservaFacturable reserva, Solicitante solicitante) {
        boolean esSuCliente = solicitante.rol() == Rol.CLIENTE && reserva.clienteId().equals(solicitante.id());
        if (!solicitante.esAdmin() && !esSuCliente && !esSuProveedor(reserva, solicitante)) {
            throw new AccesoDenegadoException("No tienes acceso a los pagos ni facturas de esta reserva");
        }
    }

    /** Permite confirmar o rechazar pagos solo al proveedor de la herramienta o a un ADMIN. */
    static void exigirGestionar(ReservaFacturable reserva, Solicitante solicitante) {
        if (!solicitante.esAdmin() && !esSuProveedor(reserva, solicitante)) {
            throw new AccesoDenegadoException("Solo el proveedor de la herramienta o un ADMIN gestionan este pago");
        }
    }

    /** Indica si el solicitante es el PROVEEDOR dueño de la herramienta de la reserva. */
    private static boolean esSuProveedor(ReservaFacturable reserva, Solicitante solicitante) {
        return solicitante.rol() == Rol.PROVEEDOR && reserva.proveedorId().equals(solicitante.id());
    }
}
