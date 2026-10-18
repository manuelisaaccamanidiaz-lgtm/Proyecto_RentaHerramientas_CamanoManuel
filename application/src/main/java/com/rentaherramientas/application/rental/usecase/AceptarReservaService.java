package com.rentaherramientas.application.rental.usecase;

import com.rentaherramientas.application.rental.dto.ReservaResponse;
import com.rentaherramientas.application.rental.port.CatalogoHerramientas;
import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.domain.rental.model.aggregate.Reserva;
import com.rentaherramientas.domain.rental.port.repository.ReservaRepository;

/**
 * Acepta una reserva aplicando la regla de propiedad; la transición la valida el agregado.
 */
public class AceptarReservaService implements AceptarReservaUseCase {

    private final ReservaRepository reservaRepository;
    private final CatalogoHerramientas catalogoHerramientas;

    /** Recibe sus dependencias por constructor; el bean lo crea RentalBeansConfig. */
    public AceptarReservaService(ReservaRepository reservaRepository, CatalogoHerramientas catalogoHerramientas) {
        this.reservaRepository = reservaRepository;
        this.catalogoHerramientas = catalogoHerramientas;
    }

    /** El proveedor de la herramienta (o un ADMIN) acepta una reserva PENDIENTE. */
    @Override
    public ReservaResponse aceptar(Long reservaId, Solicitante solicitante) {
        Reserva reserva = PropiedadReserva.cargarParaGestionar(reservaRepository, catalogoHerramientas,
                reservaId, solicitante);
        reserva.aceptar();
        // Venía de PENDIENTE, que no admite pagos: recién aceptada aún no está pagada.
        return ReservaResponse.desde(reservaRepository.guardar(reserva), false);
    }
}
