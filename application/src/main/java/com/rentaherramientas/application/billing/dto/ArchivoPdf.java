package com.rentaherramientas.application.billing.dto;

/**
 * PDF generado, con el nombre de archivo sugerido para la descarga.
 */
public record ArchivoPdf(String nombreArchivo, byte[] contenido) {

    /**
     * Muestra solo el nombre y el tamaño, no los bytes del PDF.
     *
     * TÉCNICA AVANZADA (Java): arrays dentro de records
     *   Qué hace: el `toString`, `equals` y `hashCode` generados para un componente `byte[]` usan la
     *     referencia del array y no su contenido; por eso se redefine `toString` para que sea legible.
     */
    @Override
    public String toString() {
        return "ArchivoPdf[nombreArchivo=" + nombreArchivo + ", bytes=" + contenido.length + "]";
    }
}
