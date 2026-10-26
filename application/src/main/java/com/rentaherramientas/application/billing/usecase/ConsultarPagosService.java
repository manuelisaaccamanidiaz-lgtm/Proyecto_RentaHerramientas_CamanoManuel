package com.rentaherramientas.application.billing.usecase;

import com.rentaherramientas.application.billing.command.FiltroHistorialPagos;
import com.rentaherramientas.application.billing.dto.PagoResponse;
import com.rentaherramientas.application.billing.exception.PagoNoEncontradoException;
import com.rentaherramientas.application.billing.port.ConsultaReservas;
import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.application.shared.dto.PaginaResponse;
import com.rentaherramientas.domain.billing.model.aggregate.Factura;
import com.rentaherramientas.domain.billing.model.aggregate.Pago;
import com.rentaherramientas.domain.billing.port.repository.CriteriosBusquedaPago;
import com.rentaherramientas.domain.billing.port.repository.FacturaRepository;
import com.rentaherramientas.domain.billing.port.repository.PagoRepository;
import com.rentaherramientas.domain.shared.model.Pagina;
import com.rentaherramientas.domain.shared.model.SolicitudPagina;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Consultas de solo lectura sobre pagos. Cada página trae las facturas de sus
 * pagos en una sola consulta adicional (no una por pago).
 */
public class ConsultarPagosService implements ConsultarPagosUseCase {

    private final PagoRepository pagoRepository;
    private final FacturaRepository facturaRepository;
    private final ConsultaReservas consultaReservas;

    /** Recibe sus dependencias por constructor; el bean lo crea BillingBeansConfig. */
    public ConsultarPagosService(PagoRepository pagoRepository, FacturaRepository facturaRepository,
                                 ConsultaReservas consultaReservas) {
        this.pagoRepository = pagoRepository;
        this.facturaRepository = facturaRepository;
        this.consultaReservas = consultaReservas;
    }

    /** Devuelve un pago (con su factura, si tiene) si el solicitante tiene acceso a su reserva. */
    @Override
    public PagoResponse porId(Long pagoId, Solicitante solicitante) {
        Pago pago = pagoRepository.buscarPorId(pagoId).orElseThrow(() -> new PagoNoEncontradoException(pagoId));
        AccesoFacturacion.exigirVer(AccesoFacturacion.cargarReserva(consultaReservas, pago.getReservaId()),
                solicitante);
        return PagoResponse.desde(pago, facturaRepository.buscarPorPagoId(pagoId).orElse(null));
    }

    /** Pagos de las reservas del cliente. */
    @Override
    public PaginaResponse<PagoResponse> misPagos(Long clienteId, int pagina, int tamano) {
        return buscar(consultaReservas.reservaIdsDelCliente(clienteId), null, pagina, tamano);
    }

    /** Pagos de las reservas de herramientas del proveedor. */
    @Override
    public PaginaResponse<PagoResponse> delProveedor(Long proveedorId, int pagina, int tamano) {
        return buscar(consultaReservas.reservaIdsDelProveedor(proveedorId), null, pagina, tamano);
    }

    /** Historial de todos los pagos con filtros de estado, método y fechas. */
    @Override
    public PaginaResponse<PagoResponse> historial(FiltroHistorialPagos filtro) {
        return buscar(null, filtro, filtro.pagina(), filtro.tamano());
    }

    /**
     * Busca la página de pagos y carga sus facturas con una sola consulta.
     *
     * TÉCNICA AVANZADA (Java): `Collectors.toMap` + `Function.identity()`
     *   Qué hace: convierte la lista de facturas en un `Map<pagoId, Factura>`; la clave sale de
     *     `Factura::getPagoId` y el valor es la propia factura. Después cada pago busca su factura en el mapa
     *     en tiempo constante, evitando N consultas (problema N+1).
     */
    private PaginaResponse<PagoResponse> buscar(Collection<Long> reservaIds, FiltroHistorialPagos filtro,
                                                int pagina, int tamano) {
        CriteriosBusquedaPago criterios = new CriteriosBusquedaPago(
                filtro == null ? null : filtro.estado(),
                filtro == null ? null : filtro.metodo(),
                reservaIds,
                filtro == null ? null : filtro.desde(),
                filtro == null ? null : filtro.hasta(),
                new SolicitudPagina(pagina, tamano));
        Pagina<Pago> pagos = pagoRepository.buscar(criterios);
        List<Long> pagoIds = pagos.contenido().stream().map(Pago::getId).toList();
        Map<Long, Factura> facturasPorPago = pagoIds.isEmpty() ? Map.of()
                : facturaRepository.buscarPorPagoIds(pagoIds).stream()
                        .collect(Collectors.toMap(Factura::getPagoId, Function.identity()));
        return PaginaResponse.desde(pagos, pago -> PagoResponse.desde(pago, facturasPorPago.get(pago.getId())));
    }
}
