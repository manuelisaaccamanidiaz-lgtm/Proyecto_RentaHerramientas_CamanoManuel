package com.rentaherramientas.application.billing.port;

import java.util.Optional;

/**
 * Puerto de salida de billing hacia identity: datos de cliente y proveedor que
 * se imprimen en la factura.
 */
public interface ConsultaTerceros {

    /** Devuelve los datos de facturación (nombre, documento, contacto) de un usuario. */
    Optional<Tercero> buscar(Long usuarioId);
}
