package com.rentaherramientas.application.rental.usecase;

import com.rentaherramientas.application.rental.exception.HerramientaNoEncontradaException;
import com.rentaherramientas.application.rental.exception.ReservaNoEncontradaException;
import com.rentaherramientas.application.rental.port.CatalogoHerramientas;
import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.application.shared.exception.AccesoDenegadoException;
import com.rentaherramientas.domain.identity.model.valueobject.Rol;
import com.rentaherramientas.domain.rental.model.aggregate.Reserva;
import com.rentaherramientas.domain.rental.port.repository.ReservaRepository;

/**
 * Reglas de acceso a una reserva, compartidas por los casos de uso de rental.
 * Gestionarla (aceptar, rechazar, entregar, devolver, reportar daños) es cosa del
 * PROVEEDOR dueño de la herramienta o de un ADMIN; verla, además, del CLIENTE
 * que la hizo. Primero se comprueba que exista (404) y luego el acceso (403).
 */
final class PropiedadReserva {

    private PropiedadReserva() {
    }

    static Reserva cargarParaGestionar(ReservaRepository reservaRepository, CatalogoHerramientas catalogo,
                                       Long reservaId, Solicitante solicitante) {
        Reserva reserva = cargar(reservaRepository, reservaId);
        if (!solicitante.esAdmin() && !esProveedorDeLaHerramienta(catalogo, reserva, solicitante)) {
            throw new AccesoDenegadoException("Solo el proveedor de la herramienta o un ADMIN gestionan esta reserva");
        }
        return reserva;
    }

    static Reserva cargarParaVer(ReservaRepository reservaRepository, CatalogoHerramientas catalogo,
                                 Long reservaId, Solicitante solicitante) {
        Reserva reserva = cargar(reservaRepository, reservaId);
        boolean esSuCliente = solicitante.rol() == Rol.CLIENTE && reserva.getClienteId().equals(solicitante.id());
        if (!solicitante.esAdmin() && !esSuCliente && !esProveedorDeLaHerramienta(catalogo, reserva, solicitante)) {
            throw new AccesoDenegadoException("No tienes acceso a esta reserva");
        }
        return reserva;
    }

    private static Reserva cargar(ReservaRepository reservaRepository, Long reservaId) {
        return reservaRepository.buscarPorId(reservaId)
                .orElseThrow(() -> new ReservaNoEncontradaException(reservaId));
    }

    private static boolean esProveedorDeLaHerramienta(CatalogoHerramientas catalogo, Reserva reserva,
                                                      Solicitante solicitante) {
        if (solicitante.rol() != Rol.PROVEEDOR) {
            return false;
        }
        Long proveedorId = catalogo.buscar(reserva.getHerramientaId())
                .orElseThrow(() -> new HerramientaNoEncontradaException(reserva.getHerramientaId()))
                .proveedorId();
        return proveedorId.equals(solicitante.id());
    }
}
