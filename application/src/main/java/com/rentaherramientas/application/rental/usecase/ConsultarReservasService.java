package com.rentaherramientas.application.rental.usecase;

import com.rentaherramientas.application.rental.command.FiltroHistorialReservas;
import com.rentaherramientas.application.rental.dto.DisponibilidadResponse;
import com.rentaherramientas.application.rental.dto.ReservaResponse;
import com.rentaherramientas.application.rental.exception.HerramientaNoEncontradaException;
import com.rentaherramientas.application.rental.port.CatalogoHerramientas;
import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.application.shared.dto.PaginaResponse;
import com.rentaherramientas.domain.rental.model.valueobject.EstadoReserva;
import com.rentaherramientas.domain.rental.model.valueobject.RangoFechas;
import com.rentaherramientas.domain.rental.port.repository.CriteriosBusquedaReserva;
import com.rentaherramientas.domain.rental.port.repository.ReservaRepository;
import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;
import com.rentaherramientas.domain.shared.model.SolicitudPagina;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

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
    private final Clock clock;

    public ConsultarReservasService(ReservaRepository reservaRepository, CatalogoHerramientas catalogoHerramientas,
                                    Clock clock) {
        this.reservaRepository = reservaRepository;
        this.catalogoHerramientas = catalogoHerramientas;
        this.clock = clock;
    }

    @Override
    public ReservaResponse porId(Long reservaId, Solicitante solicitante) {
        return ReservaResponse.desde(PropiedadReserva.cargarParaVer(reservaRepository, catalogoHerramientas,
                reservaId, solicitante));
    }

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

    @Override
    public PaginaResponse<ReservaResponse> historial(FiltroHistorialReservas filtro) {
        return buscar(new CriteriosBusquedaReserva(filtro.estado(), filtro.clienteId(), filtro.herramientaId(),
                null, filtro.desde(), filtro.hasta(), new SolicitudPagina(filtro.pagina(), filtro.tamano())));
    }

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

    private PaginaResponse<ReservaResponse> buscar(CriteriosBusquedaReserva criterios) {
        return PaginaResponse.desde(reservaRepository.buscar(criterios), ReservaResponse::desde);
    }
}
