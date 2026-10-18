package com.rentaherramientas.application.rental.usecase;

import com.rentaherramientas.application.rental.command.FiltroHistorialReservas;
import com.rentaherramientas.application.rental.dto.DisponibilidadResponse;
import com.rentaherramientas.application.rental.dto.ReservaResponse;
import com.rentaherramientas.application.rental.exception.HerramientaNoEncontradaException;
import com.rentaherramientas.application.rental.port.CatalogoHerramientas;
import com.rentaherramientas.application.rental.port.ConsultaPagos;
import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.application.shared.dto.PaginaResponse;
import com.rentaherramientas.domain.rental.model.aggregate.Reserva;
import com.rentaherramientas.domain.rental.model.valueobject.EstadoReserva;
import com.rentaherramientas.domain.rental.model.valueobject.RangoFechas;
import com.rentaherramientas.domain.rental.port.repository.CriteriosBusquedaReserva;
import com.rentaherramientas.domain.rental.port.repository.ReservaRepository;
import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;
import com.rentaherramientas.domain.shared.model.Pagina;
import com.rentaherramientas.domain.shared.model.SolicitudPagina;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Consultas de solo lectura sobre reservas; siempre devuelve DTOs. El periodo
 * de disponibilidad se limita a {@value #DISPONIBILIDAD_MAX_DIAS} días para
 * acotar la consulta de un endpoint público.
 */
public class ConsultarReservasService implements ConsultarReservasUseCase {

    static final int DISPONIBILIDAD_DIAS_POR_DEFECTO = 90;
    static final int DISPONIBILIDAD_MAX_DIAS = 366;

    private final ReservaRepository reservaRepository;
    private final CatalogoHerramientas catalogoHerramientas;
    private final ConsultaPagos consultaPagos;
    private final Clock clock;

    /**
     * Recibe sus dependencias por constructor; el bean lo crea RentalBeansConfig.
     *
     * TÉCNICA AVANZADA (Java): inyectar `java.time.Clock`
     *   Qué hace: en lugar de llamar a `LocalDate.now()` directamente, se usa `LocalDate.now(clock)`. En
     *     producción el reloj es el real; en las pruebas se pasa `Clock.fixed(...)` para fijar "hoy" y que
     *     los resultados no dependan del día en que se ejecutan.
     */
    public ConsultarReservasService(ReservaRepository reservaRepository, CatalogoHerramientas catalogoHerramientas,
                                    ConsultaPagos consultaPagos, Clock clock) {
        this.reservaRepository = reservaRepository;
        this.catalogoHerramientas = catalogoHerramientas;
        this.consultaPagos = consultaPagos;
        this.clock = clock;
    }

    /** Devuelve una reserva si el solicitante es su cliente, el proveedor de la herramienta o un ADMIN. */
    @Override
    public ReservaResponse porId(Long reservaId, Solicitante solicitante) {
        Reserva reserva = PropiedadReserva.cargarParaVer(reservaRepository, catalogoHerramientas, reservaId,
                solicitante);
        return ReservaResponse.desde(reserva, consultaPagos.tienePagoAprobado(reservaId));
    }

    /** Reservas del cliente, opcionalmente filtradas por estado. */
    @Override
    public PaginaResponse<ReservaResponse> misReservas(Long clienteId, EstadoReserva estado, int pagina, int tamano) {
        return buscar(new CriteriosBusquedaReserva(estado, clienteId, null, null, null, null,
                new SolicitudPagina(pagina, tamano)));
    }

    /** Un proveedor sin herramientas recibe una página vacía (herramientaIds vacío no devuelve nada). */
    @Override
    public PaginaResponse<ReservaResponse> delProveedor(Long proveedorId, EstadoReserva estado, int pagina,
                                                        int tamano) {
        List<Long> herramientaIds = catalogoHerramientas.idsDelProveedor(proveedorId);
        return buscar(new CriteriosBusquedaReserva(estado, null, null, herramientaIds, null, null,
                new SolicitudPagina(pagina, tamano)));
    }

    /** Historial completo con filtros (estado, cliente, herramienta y fechas). */
    @Override
    public PaginaResponse<ReservaResponse> historial(FiltroHistorialReservas filtro) {
        return buscar(new CriteriosBusquedaReserva(filtro.estado(), filtro.clienteId(), filtro.herramientaId(),
                null, filtro.desde(), filtro.hasta(), new SolicitudPagina(filtro.pagina(), filtro.tamano())));
    }

    /**
     * Devuelve los rangos ocupados de una herramienta en un periodo (por defecto desde hoy) para
     * pintar el calendario; limita el periodo a DISPONIBILIDAD_MAX_DIAS.
     */
    @Override
    public DisponibilidadResponse disponibilidad(Long herramientaId, LocalDate desde, LocalDate hasta) {
        if (catalogoHerramientas.buscar(herramientaId).isEmpty()) {
            throw new HerramientaNoEncontradaException(herramientaId);
        }
        LocalDate inicio = desde == null ? LocalDate.now(clock) : desde;
        LocalDate fin = hasta == null ? inicio.plusDays(DISPONIBILIDAD_DIAS_POR_DEFECTO - 1) : hasta;
        RangoFechas periodo = new RangoFechas(inicio, fin);
        if (periodo.dias() > DISPONIBILIDAD_MAX_DIAS) {
            throw new ValorInvalidoException(
                    "El periodo de disponibilidad no puede superar " + DISPONIBILIDAD_MAX_DIAS + " días");
        }
        List<DisponibilidadResponse.RangoOcupado> ocupados = reservaRepository.rangosOcupados(herramientaId, periodo)
                .stream()
                .map(rango -> new DisponibilidadResponse.RangoOcupado(rango.inicio(), rango.fin()))
                .toList();
        return new DisponibilidadResponse(herramientaId, inicio, fin, ocupados);
    }

    /**
     * Busca la página y consulta en un solo viaje qué reservas de la página están pagadas.
     *
     * Técnica avanzada — evitar el problema N+1: en vez de preguntar a billing una vez por cada reserva (N
     *   consultas), se envían todos los ids juntos (`reservasPagadas`) y se usa un `Set` para comprobar cada
     *   una en tiempo constante.
     */
    private PaginaResponse<ReservaResponse> buscar(CriteriosBusquedaReserva criterios) {
        Pagina<Reserva> pagina = reservaRepository.buscar(criterios);
        Set<Long> pagadas = pagina.contenido().isEmpty() ? Set.of()
                : consultaPagos.reservasPagadas(pagina.contenido().stream().map(Reserva::getId).toList());
        return PaginaResponse.desde(pagina, reserva -> ReservaResponse.desde(reserva,
                pagadas.contains(reserva.getId())));
    }
}
