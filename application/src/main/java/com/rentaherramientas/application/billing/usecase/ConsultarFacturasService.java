package com.rentaherramientas.application.billing.usecase;

import com.rentaherramientas.application.billing.dto.ArchivoPdf;
import com.rentaherramientas.application.billing.dto.FacturaResponse;
import com.rentaherramientas.application.billing.exception.FacturaNoEncontradaException;
import com.rentaherramientas.application.billing.exception.PagoNoEncontradoException;
import com.rentaherramientas.application.billing.port.ConsultaReservas;
import com.rentaherramientas.application.billing.port.ConsultaTerceros;
import com.rentaherramientas.application.billing.port.DocumentoFactura;
import com.rentaherramientas.application.billing.port.GeneradorPdfFactura;
import com.rentaherramientas.application.billing.port.ReservaFacturable;
import com.rentaherramientas.application.billing.port.Tercero;
import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.application.shared.dto.PaginaResponse;
import com.rentaherramientas.domain.billing.model.aggregate.Factura;
import com.rentaherramientas.domain.billing.model.aggregate.Pago;
import com.rentaherramientas.domain.billing.port.repository.FacturaRepository;
import com.rentaherramientas.domain.billing.port.repository.PagoRepository;
import com.rentaherramientas.domain.shared.model.SolicitudPagina;

import java.util.Collection;

/**
 * Consultas de solo lectura sobre facturas. Para verificar el acceso se recorre
 * factura → pago → reserva, porque la factura no guarda la reserva (3FN).
 */
public class ConsultarFacturasService implements ConsultarFacturasUseCase {

    private final FacturaRepository facturaRepository;
    private final PagoRepository pagoRepository;
    private final ConsultaReservas consultaReservas;
    private final ConsultaTerceros consultaTerceros;
    private final GeneradorPdfFactura generadorPdfFactura;

    public ConsultarFacturasService(FacturaRepository facturaRepository, PagoRepository pagoRepository,
                                    ConsultaReservas consultaReservas, ConsultaTerceros consultaTerceros,
                                    GeneradorPdfFactura generadorPdfFactura) {
        this.facturaRepository = facturaRepository;
        this.pagoRepository = pagoRepository;
        this.consultaReservas = consultaReservas;
        this.consultaTerceros = consultaTerceros;
        this.generadorPdfFactura = generadorPdfFactura;
    }

    @Override
    public FacturaResponse porId(Long facturaId, Solicitante solicitante) {
        return FacturaResponse.desde(cargarConAcceso(facturaId, solicitante).factura());
    }

    @Override
    public PaginaResponse<FacturaResponse> misFacturas(Long clienteId, int pagina, int tamano) {
        return buscar(consultaReservas.reservaIdsDelCliente(clienteId), pagina, tamano);
    }

    @Override
    public PaginaResponse<FacturaResponse> delProveedor(Long proveedorId, int pagina, int tamano) {
        return buscar(consultaReservas.reservaIdsDelProveedor(proveedorId), pagina, tamano);
    }

    @Override
    public PaginaResponse<FacturaResponse> todas(int pagina, int tamano) {
        return buscar(null, pagina, tamano);
    }

    @Override
    public ArchivoPdf pdf(Long facturaId, Solicitante solicitante) {
        FacturaConContexto contexto = cargarConAcceso(facturaId, solicitante);
        ReservaFacturable reserva = contexto.reserva();
        DocumentoFactura documento = new DocumentoFactura(
                FacturaResponse.desde(contexto.factura()),
                tercero(reserva.clienteId()),
                tercero(reserva.proveedorId()),
                contexto.pago().getReferencia().valor(),
                contexto.pago().getMetodo());
        return new ArchivoPdf(contexto.factura().getNumero().valor() + ".pdf",
                generadorPdfFactura.generar(documento));
    }

    private FacturaConContexto cargarConAcceso(Long facturaId, Solicitante solicitante) {
        Factura factura = facturaRepository.buscarPorId(facturaId)
                .orElseThrow(() -> new FacturaNoEncontradaException(facturaId));
        Pago pago = pagoRepository.buscarPorId(factura.getPagoId())
                .orElseThrow(() -> new PagoNoEncontradoException(factura.getPagoId()));
        ReservaFacturable reserva = AccesoFacturacion.cargarReserva(consultaReservas, pago.getReservaId());
        AccesoFacturacion.exigirVer(reserva, solicitante);
        return new FacturaConContexto(factura, pago, reserva);
    }

    /** Si el usuario ya no existe, la factura se imprime igual con solo su id. */
    private Tercero tercero(Long usuarioId) {
        return consultaTerceros.buscar(usuarioId)
                .orElse(new Tercero(usuarioId, "Usuario " + usuarioId, null, null, null, null, null));
    }

    private PaginaResponse<FacturaResponse> buscar(Collection<Long> reservaIds, int pagina, int tamano) {
        return PaginaResponse.desde(facturaRepository.buscar(reservaIds, new SolicitudPagina(pagina, tamano)),
                FacturaResponse::desde);
    }

    private record FacturaConContexto(Factura factura, Pago pago, ReservaFacturable reserva) {
    }
}
