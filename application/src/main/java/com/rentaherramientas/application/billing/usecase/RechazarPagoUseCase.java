package com.rentaherramientas.application.billing.usecase;

import com.rentaherramientas.application.billing.dto.PagoResponse;
import com.rentaherramientas.application.shared.command.Solicitante;

/**
 * El proveedor de la herramienta (o un ADMIN) rechaza un pago PENDIENTE; el
 * cliente puede volver a pagar la reserva.
 */
public interface RechazarPagoUseCase {

    /** Rechaza el pago indicado. */
    PagoResponse rechazar(Long pagoId, Solicitante solicitante);
}
