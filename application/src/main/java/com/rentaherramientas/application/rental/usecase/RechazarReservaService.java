package com.rentaherramientas.application.rental.usecase;

import com.rentaherramientas.application.rental.dto.ReservaResponse;
import com.rentaherramientas.application.rental.port.CatalogoHerramientas;
import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.domain.rental.model.aggregate.Reserva;
import com.rentaherramientas.domain.rental.port.repository.ReservaRepository;

/**
 * Rechaza una reserva aplicando la regla de propiedad; al pasar a RECHAZADA deja
 * de bloquear las fechas de la herramienta.
 */
public class RechazarReservaService implements RechazarReservaUseCase {

    private final ReservaRepository reservaRepository;
    private final CatalogoHerramientas catalogoHerramientas;

    /** Recibe sus dependencias por constructor; el bean lo crea RentalBeansConfig. */
    public RechazarReservaService(ReservaRepository reservaRepository, CatalogoHerramientas catalogoHerramientas) {
        this.reservaRepository = reservaRepository;
        this.catalogoHerramientas = catalogoHerramientas;
    }

    /** El proveedor de la herramienta (o un ADMIN) rechaza una reserva PENDIENTE. */
    @Override
    public ReservaResponse rechazar(Long reservaId, Solicitante solicitante) {
        Reserva reserva = PropiedadReserva.cargarParaGestionar(reservaRepository, catalogoHerramientas,
                reservaId, solicitante);
        reserva.rechazar();
        // Solo se rechaza una reserva PENDIENTE, que no admite pagos.
        return ReservaResponse.desde(reservaRepository.guardar(reserva), false);
    }
}
