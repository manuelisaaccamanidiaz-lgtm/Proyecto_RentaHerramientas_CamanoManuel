package com.rentaherramientas.domain.rental.port.repository;

import com.rentaherramientas.domain.rental.model.valueobject.EstadoReserva;
import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;
import com.rentaherramientas.domain.shared.model.SolicitudPagina;
import com.rentaherramientas.domain.shared.model.Validaciones;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * Filtros opcionales (null = sin filtrar) para buscar reservas.
 * - herramientaIds restringe a un conjunto de herramientas (p. ej. las de un
 *   proveedor); una colección vacía no devuelve nada.
 * - desde/hasta devuelven las reservas cuyo periodo se cruza con ese intervalo.
 */
public record CriteriosBusquedaReserva(EstadoReserva estado,
                                       Long clienteId,
                                       Long herramientaId,
                                       Collection<Long> herramientaIds,
                                       LocalDate desde,
                                       LocalDate hasta,
                                       SolicitudPagina pagina) {

    public CriteriosBusquedaReserva {
        herramientaIds = herramientaIds == null ? null : List.copyOf(herramientaIds);
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            throw new ValorInvalidoException("La fecha 'hasta' no puede ser anterior a 'desde'");
        }
        Validaciones.requerido(pagina, "pagina");
    }
}
