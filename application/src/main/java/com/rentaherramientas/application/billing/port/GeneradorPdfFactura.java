package com.rentaherramientas.application.billing.port;

/**
 * Puerto de salida que convierte una factura en un documento PDF. La aplicación
 * decide qué datos lleva; la librería y el diseño los elige infraestructura.
 */
public interface GeneradorPdfFactura {

    byte[] generar(DocumentoFactura documento);
}
