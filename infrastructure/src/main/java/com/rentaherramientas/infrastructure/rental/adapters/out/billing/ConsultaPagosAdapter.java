package com.rentaherramientas.infrastructure.rental.adapters.out.billing;

import com.rentaherramientas.application.rental.port.ConsultaPagos;
import com.rentaherramientas.domain.billing.port.repository.PagoRepository;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;

/**
 * Adaptador anticorrupción de rental hacia billing: responde si las reservas
 * están pagadas usando el repositorio de pagos. Es el único punto donde rental
 * "toca" billing.
 */
@Component
public class ConsultaPagosAdapter implements ConsultaPagos {

    private final PagoRepository pagoRepository;

    /** Recibe el repositorio de pagos de billing por constructor. */
    public ConsultaPagosAdapter(PagoRepository pagoRepository) {
        this.pagoRepository = pagoRepository;
    }

    /** Indica si la reserva tiene un pago APROBADO. */
    @Override
    public boolean tienePagoAprobado(Long reservaId) {
        return pagoRepository.existePagoAprobado(reservaId);
    }

    /** De las reservas indicadas, devuelve los ids de las que tienen pago APROBADO (una sola consulta). */
    @Override
    public Set<Long> reservasPagadas(Collection<Long> reservaIds) {
        return pagoRepository.reservasConPagoAprobado(reservaIds);
    }
}
