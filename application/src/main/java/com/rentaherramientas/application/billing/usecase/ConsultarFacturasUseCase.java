package com.rentaherramientas.application.billing.usecase;

import com.rentaherramientas.application.billing.dto.ArchivoPdf;
import com.rentaherramientas.application.billing.dto.FacturaResponse;
import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.application.shared.dto.PaginaResponse;

/**
 * Consultas de facturas y su descarga en PDF. Una factura la ven el cliente de
 * la reserva, el proveedor de la herramienta o un ADMIN.
 */
public interface ConsultarFacturasUseCase {

    FacturaResponse porId(Long facturaId, Solicitante solicitante);

    PaginaResponse<FacturaResponse> misFacturas(Long clienteId, int pagina, int tamano);

    PaginaResponse<FacturaResponse> delProveedor(Long proveedorId, int pagina, int tamano);

    PaginaResponse<FacturaResponse> todas(int pagina, int tamano);

    ArchivoPdf pdf(Long facturaId, Solicitante solicitante);
}
