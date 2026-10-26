package com.rentaherramientas.application.billing.usecase;

import com.rentaherramientas.application.billing.command.FiltroHistorialPagos;
import com.rentaherramientas.application.billing.dto.PagoResponse;
import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.application.shared.dto.PaginaResponse;

/**
 * Consultas de pagos para cada actor: el cliente (los suyos), el proveedor (los
 * de reservas de sus herramientas) y el ADMIN (historial con filtros).
 */
public interface ConsultarPagosUseCase {

    /** Visible para el cliente de la reserva, el proveedor de la herramienta o un ADMIN. */
    PagoResponse porId(Long pagoId, Solicitante solicitante);

    /** Pagos del cliente indicado. */
    PaginaResponse<PagoResponse> misPagos(Long clienteId, int pagina, int tamano);

    /** Pagos de las herramientas del proveedor indicado. */
    PaginaResponse<PagoResponse> delProveedor(Long proveedorId, int pagina, int tamano);

    /** Historial de pagos con filtros. */
    PaginaResponse<PagoResponse> historial(FiltroHistorialPagos filtro);
}
