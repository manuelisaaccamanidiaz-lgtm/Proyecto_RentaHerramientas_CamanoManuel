package com.rentaherramientas.application.rental.usecase;

import com.rentaherramientas.application.rental.command.CrearReservaCommand;
import com.rentaherramientas.application.rental.dto.ReservaResponse;
import com.rentaherramientas.application.rental.exception.HerramientaNoDisponibleException;
import com.rentaherramientas.application.rental.exception.HerramientaNoEncontradaException;
import com.rentaherramientas.application.rental.exception.LimiteReservasPendientesException;
import com.rentaherramientas.application.rental.port.CatalogoHerramientas;
import com.rentaherramientas.application.rental.port.HerramientaReservable;
import com.rentaherramientas.application.shared.exception.AccesoDenegadoException;
import com.rentaherramientas.application.shared.port.PublicadorEventos;
import com.rentaherramientas.application.shared.port.UnidadDeTrabajo;
import com.rentaherramientas.domain.identity.model.valueobject.Rol;
import com.rentaherramientas.domain.rental.exception.ReservaSolapadaException;
import com.rentaherramientas.domain.rental.model.aggregate.Reserva;
import com.rentaherramientas.domain.rental.model.valueobject.EstadoReserva;
import com.rentaherramientas.domain.rental.model.valueobject.RangoFechas;
import com.rentaherramientas.domain.rental.port.repository.ReservaRepository;
import com.rentaherramientas.domain.shared.model.Validaciones;

import java.time.Clock;

/**
 * Registra una reserva verificando, en este orden: que el solicitante sea
 * CLIENTE, que no tenga ya el máximo de reservas PENDIENTES (409), que la herramienta exista (404) y esté DISPONIBLE (409), que empiece
 * hoy o después y no dure más de {@value ReglasFechasReserva#DURACION_MAXIMA_DIAS}
 * días (400, reglas compartidas con reprogramar) y que no se cruce con otra reserva activa (409). Esta última
 * comprobación tiene una carrera entre dos altas simultáneas; la cierra la
 * restricción de exclusión de la BD, que el repositorio traduce a la misma excepción.
 * Guardar y publicar ReservaSolicitada van en la misma unidad de trabajo.
 */
public class CrearReservaService implements CrearReservaUseCase {

    private final ReservaRepository reservaRepository;
    private final CatalogoHerramientas catalogoHerramientas;
    private final UnidadDeTrabajo unidadDeTrabajo;
    private final PublicadorEventos publicadorEventos;
    private final Clock clock;
    private final int maxReservasPendientes;

    /** Recibe sus dependencias por constructor; el bean lo crea RentalBeansConfig. */
    public CrearReservaService(ReservaRepository reservaRepository, CatalogoHerramientas catalogoHerramientas,
                               UnidadDeTrabajo unidadDeTrabajo, PublicadorEventos publicadorEventos,
                               Clock clock, int maxReservasPendientes) {
        this.reservaRepository = reservaRepository;
        this.catalogoHerramientas = catalogoHerramientas;
        this.unidadDeTrabajo = unidadDeTrabajo;
        this.publicadorEventos = publicadorEventos;
        this.clock = clock;
        this.maxReservasPendientes = maxReservasPendientes;
    }

    /**
     * Crea una reserva PENDIENTE: solo un CLIENTE, herramienta disponible, fechas desde hoy,
     * duración máxima y sin solaparse con otras reservas.
     *
     * Nota: La comprobación `existeSolapamiento` da un error claro en el caso normal, pero dos peticiones
     *   simultáneas podrían pasarla a la vez. La garantía definitiva es la restricción EXCLUDE de PostgreSQL
     *   (migración V5), que el adaptador traduce a la misma excepción.
     */
    @Override
    public ReservaResponse crear(CrearReservaCommand command) {
        if (command.solicitante().rol() != Rol.CLIENTE) {
            throw new AccesoDenegadoException("Solo un CLIENTE puede crear reservas");
        }
        // Solo cuentan las PENDIENTE: las ya aceptadas o cerradas no saturan al proveedor.
        if (reservaRepository.contarPorClienteYEstado(command.solicitante().id(), EstadoReserva.PENDIENTE)
                >= maxReservasPendientes) {
            throw new LimiteReservasPendientesException(maxReservasPendientes);
        }
        Long herramientaId = Validaciones.requerido(command.herramientaId(), "herramientaId");
        HerramientaReservable herramienta = catalogoHerramientas.buscar(herramientaId)
                .orElseThrow(() -> new HerramientaNoEncontradaException(herramientaId));
        if (!herramienta.disponible()) {
            throw new HerramientaNoDisponibleException(herramientaId);
        }

        RangoFechas rango = new RangoFechas(command.fechaInicio(), command.fechaFin());
        ReglasFechasReserva.validar(rango, clock);
        if (reservaRepository.existeSolapamiento(herramientaId, rango)) {
            throw new ReservaSolapadaException(herramientaId, rango);
        }

        Reserva reserva = Reserva.registrar(herramientaId, command.solicitante().id(), rango,
                herramienta.tarifaDiaria());
        return unidadDeTrabajo.ejecutar(() -> {
            Reserva guardada = reservaRepository.guardar(reserva);
            publicadorEventos.publicar(reserva.extraerEventos());
            // Una reserva recién creada no puede tener pagos: solo se pagan las ACEPTADAS.
            return ReservaResponse.desde(guardada, false);
        });
    }
}
