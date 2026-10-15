package com.rentaherramientas.application.catalog.port;

/**
 * Puerto de salida para guardar los archivos de imagen de las herramientas. La
 * aplicación solo maneja la URL pública resultante; dónde y cómo se guardan los
 * bytes (disco local, S3...) lo decide infraestructura.
 */
public interface AlmacenamientoImagenes {

    /** Guarda el archivo y devuelve su URL pública: absoluta http/https o ruta relativa que empieza con "/". */
    String guardar(byte[] contenido, String contentType);

    /** Borra el archivo de esa URL; si no existe o no es de este almacenamiento, no hace nada. */
    void eliminar(String url);
}
