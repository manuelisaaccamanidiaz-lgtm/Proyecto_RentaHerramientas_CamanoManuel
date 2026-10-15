package com.rentaherramientas.application.rental.usecase;

import com.rentaherramientas.application.rental.command.CrearReservaCommand;
import com.rentaherramientas.application.rental.dto.ReservaResponse;
import com.rentaherramientas.application.rental.exception.HerramientaNoDisponibleException;
import com.rentaherramientas.application.rental.exception.HerramientaNoEncontradaException;
import com.rentaherramientas.application.rental.port.CatalogoHerramientas;
import com.rentaherramientas.application.rental.port.HerramientaReservable;
import com.rentaherramientas.application.shared.exception.AccesoDenegadoException;
import com.rentaherramientas.domain.identity.model.valueobject.Rol;
import com.rentaherramientas.domain.rental.exception.ReservaSolapadaException;
import com.rentaherramientas.domain.rental.model.aggregate.Reserva;
import com.rentaherramientas.domain.rental.model.valueobject.RangoFechas;
import com.rentaherramientas.domain.rental.port.repository.ReservaRepository;
import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;
import com.rentaherramientas.domain.shared.model.Validaciones;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Registra una reserva verificando, en este orden: que el solicitante sea
 * CLIENTE, que la herramienta exista (404) y esté DISPONIBLE (409), que empiece
 * hoy o después (400), que no dure más de {@value #DURACION_MAXIMA_DIAS} días
 * (400) y que no se cruce con otra reserva activa (409). Esta última
 * comprobación tiene una carrera entre dos altas simultáneas; la cierra la
 * restricción de exclusión de la BD, que el repositorio traduce a la misma excepción.
 */
public class CrearReservaService implements CrearReservaUseCase {

    static final int DURACION_MAXIMA_DIAS = 30;

    private final ReservaRepository reservaRepository;
    private final CatalogoHerramientas catalogoHerramientas;
    private final Clock clock;

    public CrearReservaService(ReservaRepository reservaRepository, CatalogoHerramientas catalogoHerramientas,
                               Clock clock) {
        this.reservaRepository = reservaRepository;
        this.catalogoHerramientas = catalogoHerramientas;
        this.clock = clock;
    }

    @Override
    public ReservaResponse crear(CrearReservaCommand command) {
        if (command.solicitante().rol() != Rol.CLIENTE) {
            throw new AccesoDenegadoException("Solo un CLIENTE puede crear reservas");
        }
        Long herramientaId = Validaciones.requerido(command.herramientaId(), "herramientaId");
        HerramientaReservable herramienta = catalogoHerramientas.buscar(herramientaId)
                .orElseThrow(() -> new HerramientaNoEncontradaException(herramientaId));
        if (!herramienta.disponible()) {
            throw new HerramientaNoDisponibleException(herramientaId);
        }

        RangoFechas rango = new RangoFechas(command.fechaInicio(), command.fechaFin());
        if (rango.inicio().isBefore(LocalDate.now(clock))) {
            throw new ValorInvalidoException("La fecha de inicio no puede ser anterior a hoy");
        }
        if (rango.dias() > DURACION_MAXIMA_DIAS) {
            throw new ValorInvalidoException("Una reserva puede durar como máximo " + DURACION_MAXIMA_DIAS + " días");
        }
        if (reservaRepository.existeSolapamiento(herramientaId, rango)) {
            throw new ReservaSolapadaException(herramientaId, rango);
        }

        Reserva reserva = Reserva.registrar(herramientaId, command.solicitante().id(), rango,
                herramienta.tarifaDiaria());
        return ReservaResponse.desde(reservaRepository.guardar(reserva));
    }
}
