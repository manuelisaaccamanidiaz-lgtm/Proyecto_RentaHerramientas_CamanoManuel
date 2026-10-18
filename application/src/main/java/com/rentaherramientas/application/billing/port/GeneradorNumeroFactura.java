package com.rentaherramientas.application.billing.port;

import com.rentaherramientas.domain.billing.model.valueobject.NumeroFactura;

/**
 * Puerto de salida que entrega el siguiente número correlativo de factura
 * (FAC-<año>-<6 dígitos>). Debe ser seguro ante emisiones simultáneas: la
 * implementación usa una SEQUENCE de la base de datos.
 */
public interface GeneradorNumeroFactura {

    /** Devuelve el siguiente número de factura del año indicado, sin repetirse nunca. */
    NumeroFactura siguiente(int anio);
}
