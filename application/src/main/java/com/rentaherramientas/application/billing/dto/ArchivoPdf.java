package com.rentaherramientas.application.billing.dto;

/**
 * PDF generado, con el nombre de archivo sugerido para la descarga.
 */
public record ArchivoPdf(String nombreArchivo, byte[] contenido) {

    @Override
    public String toString() {
        return "ArchivoPdf[nombreArchivo=" + nombreArchivo + ", bytes=" + contenido.length + "]";
    }
}
