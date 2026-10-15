package com.rentaherramientas.application.billing.usecase;

import com.rentaherramientas.application.billing.dto.PagoResponse;
import com.rentaherramientas.application.shared.command.Solicitante;

/**
 * El proveedor de la herramienta (o un ADMIN) confirma que recibió un pago en
 * efectivo PENDIENTE; al aprobarse se emite la factura.
 */
public interface ConfirmarPagoUseCase {

    PagoResponse confirmar(Long pagoId, Solicitante solicitante);
}
