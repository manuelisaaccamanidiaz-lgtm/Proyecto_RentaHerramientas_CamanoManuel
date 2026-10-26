package com.rentaherramientas.domain.billing.port.repository;

import com.rentaherramientas.domain.billing.model.valueobject.EstadoPago;
import com.rentaherramientas.domain.billing.model.valueobject.MetodoPago;
import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;
import com.rentaherramientas.domain.shared.model.SolicitudPagina;
import com.rentaherramientas.domain.shared.model.Validaciones;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * Filtros opcionales (null = sin filtrar) para buscar pagos.
 * - reservaIds restringe a las reservas de un cliente o de un proveedor; una
 *   colección vacía no devuelve nada.
 * - desde/hasta filtran por la fecha del pago (ambos días incluidos).
 */
public record CriteriosBusquedaPago(EstadoPago estado,
                                    MetodoPago metodo,
                                    Collection<Long> reservaIds,
                                    LocalDate desde,
                                    LocalDate hasta,
                                    SolicitudPagina pagina) {

    public CriteriosBusquedaPago {
        reservaIds = reservaIds == null ? null : List.copyOf(reservaIds);
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            throw new ValorInvalidoException("La fecha 'hasta' no puede ser anterior a 'desde'");
        }
        Validaciones.requerido(pagina, "pagina");
    }
}
