package com.rentaherramientas.application.rental.usecase;

import com.rentaherramientas.application.rental.command.FiltroHistorialReservas;
import com.rentaherramientas.application.rental.dto.DisponibilidadResponse;
import com.rentaherramientas.application.rental.dto.ReservaResponse;
import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.application.shared.dto.PaginaResponse;
import com.rentaherramientas.domain.rental.model.valueobject.EstadoReserva;

import java.time.LocalDate;

/**
 * Consultas de reservas para cada actor: el cliente (las suyas), el proveedor
 * (las de sus herramientas), el ADMIN (historial completo) y cualquiera
 * (fechas ocupadas de una herramienta). estado null = todos los estados.
 */
public interface ConsultarReservasUseCase {

    /** Visible para el cliente que la hizo, el proveedor de la herramienta o un ADMIN. */
    ReservaResponse porId(Long reservaId, Solicitante solicitante);

    PaginaResponse<ReservaResponse> misReservas(Long clienteId, EstadoReserva estado, int pagina, int tamano);

    PaginaResponse<ReservaResponse> delProveedor(Long proveedorId, EstadoReserva estado, int pagina, int tamano);

    PaginaResponse<ReservaResponse> historial(FiltroHistorialReservas filtro);

    /** desde null = hoy; hasta null = desde + 89 días. */
    DisponibilidadResponse disponibilidad(Long herramientaId, LocalDate desde, LocalDate hasta);
}
