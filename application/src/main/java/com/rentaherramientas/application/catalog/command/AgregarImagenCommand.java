package com.rentaherramientas.application.catalog.command;

import com.rentaherramientas.application.shared.command.Solicitante;

/**
 * Archivo de imagen subido para una herramienta: contenido y tipo MIME declarado
 * por el cliente (el caso de uso verifica que el contenido coincida).
 */
public record AgregarImagenCommand(Long herramientaId, Solicitante solicitante, byte[] contenido,
                                   String contentType) {

    @Override
    public String toString() {
        return "AgregarImagenCommand[herramientaId=" + herramientaId + ", contentType=" + contentType
                + ", bytes=" + (contenido == null ? 0 : contenido.length) + "]";
    }
}
