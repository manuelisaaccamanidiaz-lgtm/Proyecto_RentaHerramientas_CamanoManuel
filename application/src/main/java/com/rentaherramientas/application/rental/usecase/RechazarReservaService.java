package com.rentaherramientas.application.rental.usecase;

import com.rentaherramientas.application.rental.dto.ReservaResponse;
import com.rentaherramientas.application.rental.port.CatalogoHerramientas;
import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.application.shared.port.PublicadorEventos;
import com.rentaherramientas.application.shared.port.UnidadDeTrabajo;
import com.rentaherramientas.domain.rental.model.aggregate.Reserva;
import com.rentaherramientas.domain.rental.port.repository.ReservaRepository;

/**
 * Rechaza una reserva aplicando la regla de propiedad; al pasar a RECHAZADA deja
 * de bloquear las fechas de la herramienta.
 */
public class RechazarReservaService implements RechazarReservaUseCase {

    private final ReservaRepository reservaRepository;
    private final CatalogoHerramientas catalogoHerramientas;
    private final UnidadDeTrabajo unidadDeTrabajo;
    private final PublicadorEventos publicadorEventos;

    /** Recibe sus dependencias por constructor; el bean lo crea RentalBeansConfig. */
    public RechazarReservaService(ReservaRepository reservaRepository, CatalogoHerramientas catalogoHerramientas,
                                  UnidadDeTrabajo unidadDeTrabajo, PublicadorEventos publicadorEventos) {
        this.reservaRepository = reservaRepository;
        this.catalogoHerramientas = catalogoHerramientas;
        this.unidadDeTrabajo = unidadDeTrabajo;
        this.publicadorEventos = publicadorEventos;
    }

    /** El proveedor de la herramienta (o un ADMIN) rechaza una reserva PENDIENTE. */
    @Override
    public ReservaResponse rechazar(Long reservaId, Solicitante solicitante) {
        Reserva reserva = PropiedadReserva.cargarParaGestionar(reservaRepository, catalogoHerramientas,
                reservaId, solicitante);
        reserva.rechazar();
        return unidadDeTrabajo.ejecutar(() -> {
            Reserva guardada = reservaRepository.guardar(reserva);
            publicadorEventos.publicar(reserva.extraerEventos());
            // Solo se rechaza una reserva PENDIENTE, que no admite pagos.
            return ReservaResponse.desde(guardada, false);
        });
    }
}
