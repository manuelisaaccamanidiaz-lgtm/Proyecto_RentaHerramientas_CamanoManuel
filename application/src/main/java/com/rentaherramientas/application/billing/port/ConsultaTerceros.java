package com.rentaherramientas.application.billing.port;

import java.util.Optional;

/**
 * Puerto de salida de billing hacia identity: datos de cliente y proveedor que
 * se imprimen en la factura.
 */
public interface ConsultaTerceros {

    Optional<Tercero> buscar(Long usuarioId);
}
