package com.rentaherramientas.application.rental.usecase;

import com.rentaherramientas.application.rental.command.ReprogramarReservaCommand;
import com.rentaherramientas.application.rental.dto.ReservaResponse;
import com.rentaherramientas.application.rental.exception.HerramientaNoEncontradaException;
import com.rentaherramientas.application.rental.port.CatalogoHerramientas;
import com.rentaherramientas.application.shared.port.PublicadorEventos;
import com.rentaherramientas.application.shared.port.UnidadDeTrabajo;
import com.rentaherramientas.domain.rental.exception.ReservaSolapadaException;
import com.rentaherramientas.domain.rental.model.aggregate.Reserva;
import com.rentaherramientas.domain.rental.model.valueobject.RangoFechas;
import com.rentaherramientas.domain.rental.port.repository.ReservaRepository;
import com.rentaherramientas.domain.shared.model.Validaciones;
import com.rentaherramientas.domain.shared.model.valueobject.Dinero;

import java.time.Clock;

/**
 * El cliente mueve su reserva PENDIENTE a otras fechas. Orden: que exista (404) y
 * sea suya (403), las mismas reglas de fechas que al crear (400), la tarifa
 * vigente del catálogo (ya con descuento) y que no se cruce con OTRA reserva
 * activa (409). Al comprobar el cruce se excluye la propia reserva: si no,
 * moverla un día chocaría consigo misma. Como al crear, la carrera entre dos
 * peticiones la cierra el EXCLUDE de la BD, que el repositorio traduce también
 * en el UPDATE. Guardar y publicar ReservaReprogramada van en la misma unidad de trabajo.
 */
public class ReprogramarReservaService implements ReprogramarReservaUseCase {

    private final ReservaRepository reservaRepository;
    private final CatalogoHerramientas catalogoHerramientas;
    private final UnidadDeTrabajo unidadDeTrabajo;
    private final PublicadorEventos publicadorEventos;
    private final Clock clock;

    /** Recibe sus dependencias por constructor; el bean lo crea RentalBeansConfig. */
    public ReprogramarReservaService(ReservaRepository reservaRepository, CatalogoHerramientas catalogoHerramientas,
                                     UnidadDeTrabajo unidadDeTrabajo, PublicadorEventos publicadorEventos,
                                     Clock clock) {
        this.reservaRepository = reservaRepository;
        this.catalogoHerramientas = catalogoHerramientas;
        this.unidadDeTrabajo = unidadDeTrabajo;
        this.publicadorEventos = publicadorEventos;
        this.clock = clock;
    }

    @Override
    public ReservaResponse reprogramar(ReprogramarReservaCommand command) {
        Long reservaId = Validaciones.requerido(command.reservaId(), "reservaId");
        Reserva reserva = PropiedadReserva.cargarDelCliente(reservaRepository, reservaId, command.solicitante());

        RangoFechas rango = new RangoFechas(command.fechaInicio(), command.fechaFin());
        ReglasFechasReserva.validar(rango, clock);

        Long herramientaId = reserva.getHerramientaId();
        Dinero tarifaActual = catalogoHerramientas.buscar(herramientaId)
                .orElseThrow(() -> new HerramientaNoEncontradaException(herramientaId))
                .tarifaDiaria();
        if (reservaRepository.existeSolapamientoExcluyendo(herramientaId, rango, reservaId)) {
            throw new ReservaSolapadaException(herramientaId, rango);
        }

        reserva.reprogramar(rango, tarifaActual);
        return unidadDeTrabajo.ejecutar(() -> {
            Reserva guardada = reservaRepository.guardar(reserva);
            publicadorEventos.publicar(reserva.extraerEventos());
            // Solo se reprograman reservas PENDIENTE, que aún no pueden tener pagos.
            return ReservaResponse.desde(guardada, false);
        });
    }
}
