package com.rentaherramientas.infrastructure.rental.adapters.in.rest;

import com.rentaherramientas.application.rental.command.FiltroHistorialReservas;
import com.rentaherramientas.application.rental.dto.ReservaResponse;
import com.rentaherramientas.application.rental.usecase.AceptarReservaUseCase;
import com.rentaherramientas.application.rental.usecase.ConsultarReservasUseCase;
import com.rentaherramientas.application.rental.usecase.CrearReservaUseCase;
import com.rentaherramientas.application.rental.usecase.RechazarReservaUseCase;
import com.rentaherramientas.application.rental.usecase.RegistrarDevolucionUseCase;
import com.rentaherramientas.application.rental.usecase.RegistrarEntregaUseCase;
import com.rentaherramientas.application.rental.usecase.ReportarDanioUseCase;
import com.rentaherramientas.application.shared.dto.PaginaResponse;
import com.rentaherramientas.domain.rental.model.valueobject.EstadoReserva;
import com.rentaherramientas.infrastructure.shared.adapters.in.rest.SolicitanteJwt;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Adaptador de entrada de reservas: alta y consulta del cliente, gestión por el
 * proveedor de la herramienta (o un ADMIN) e historial del ADMIN. SecurityConfig
 * filtra por rol; quién es dueño de qué lo deciden los casos de uso.
 */
@RestController
@Tag(name = "Reservas", description = "Reservas de herramientas y su ciclo de vida")
public class ReservaController {

    private static final String TAMANO_POR_DEFECTO = "20";

    private final CrearReservaUseCase crearReserva;
    private final AceptarReservaUseCase aceptarReserva;
    private final RechazarReservaUseCase rechazarReserva;
    private final RegistrarEntregaUseCase registrarEntrega;
    private final RegistrarDevolucionUseCase registrarDevolucion;
    private final ReportarDanioUseCase reportarDanio;
    private final ConsultarReservasUseCase consultarReservas;

    public ReservaController(CrearReservaUseCase crearReserva, AceptarReservaUseCase aceptarReserva,
                             RechazarReservaUseCase rechazarReserva, RegistrarEntregaUseCase registrarEntrega,
                             RegistrarDevolucionUseCase registrarDevolucion, ReportarDanioUseCase reportarDanio,
                             ConsultarReservasUseCase consultarReservas) {
        this.crearReserva = crearReserva;
        this.aceptarReserva = aceptarReserva;
        this.rechazarReserva = rechazarReserva;
        this.registrarEntrega = registrarEntrega;
        this.registrarDevolucion = registrarDevolucion;
        this.reportarDanio = reportarDanio;
        this.consultarReservas = consultarReservas;
    }

    @PostMapping("/api/reservas")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Solicita una reserva (solo CLIENTE); queda PENDIENTE",
            description = "Máximo 30 días, desde hoy en adelante, sin cruzarse con otras reservas activas.")
    public ReservaResponse crear(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CrearReservaRequest request) {
        return crearReserva.crear(request.toCommand(SolicitanteJwt.desde(jwt)));
    }

    @GetMapping("/api/reservas/mias")
    @Operation(summary = "Reservas del cliente autenticado (solo CLIENTE)")
    public PaginaResponse<ReservaResponse> misReservas(@AuthenticationPrincipal Jwt jwt,
                                                       @RequestParam(required = false) EstadoReserva estado,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = TAMANO_POR_DEFECTO) int size) {
        return consultarReservas.misReservas(Long.valueOf(jwt.getSubject()), estado, page, size);
    }

    @GetMapping("/api/reservas/{id}")
    @Operation(summary = "Detalle de una reserva (su cliente, el proveedor de la herramienta o un ADMIN)")
    public ReservaResponse porId(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return consultarReservas.porId(id, SolicitanteJwt.desde(jwt));
    }

    @GetMapping("/api/proveedor/reservas")
    @Operation(summary = "Reservas de las herramientas del proveedor autenticado (solo PROVEEDOR)")
    public PaginaResponse<ReservaResponse> delProveedor(@AuthenticationPrincipal Jwt jwt,
                                                        @RequestParam(required = false) EstadoReserva estado,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = TAMANO_POR_DEFECTO) int size) {
        return consultarReservas.delProveedor(Long.valueOf(jwt.getSubject()), estado, page, size);
    }

    @GetMapping("/api/admin/reservas")
    @Operation(summary = "Historial de reservas con filtros (solo ADMIN)",
            description = "desde/hasta devuelven las reservas cuyo periodo se cruza con ese intervalo.")
    public PaginaResponse<ReservaResponse> historial(
            @RequestParam(required = false) EstadoReserva estado,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Long herramientaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = TAMANO_POR_DEFECTO) int size) {
        return consultarReservas.historial(
                new FiltroHistorialReservas(estado, clienteId, herramientaId, desde, hasta, page, size));
    }

    @PatchMapping("/api/reservas/{id}/aceptar")
    @Operation(summary = "Acepta una reserva PENDIENTE (proveedor dueño o ADMIN)")
    public ReservaResponse aceptar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return aceptarReserva.aceptar(id, SolicitanteJwt.desde(jwt));
    }

    @PatchMapping("/api/reservas/{id}/rechazar")
    @Operation(summary = "Rechaza una reserva PENDIENTE (proveedor dueño o ADMIN)")
    public ReservaResponse rechazar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return rechazarReserva.rechazar(id, SolicitanteJwt.desde(jwt));
    }

    @PatchMapping("/api/reservas/{id}/entregar")
    @Operation(summary = "Registra la entrega de una reserva ACEPTADA (proveedor dueño o ADMIN)")
    public ReservaResponse entregar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return registrarEntrega.entregar(id, SolicitanteJwt.desde(jwt));
    }

    @PatchMapping("/api/reservas/{id}/devolver")
    @Operation(summary = "Registra la devolución de una reserva ENTREGADA (proveedor dueño o ADMIN)")
    public ReservaResponse devolver(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return registrarDevolucion.devolver(id, SolicitanteJwt.desde(jwt));
    }

    @PostMapping("/api/reservas/{id}/danios")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Reporta un daño en una reserva DEVUELTA y marca la herramienta como DANADA "
            + "(proveedor dueño o ADMIN)")
    public ReservaResponse reportarDanio(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                         @Valid @RequestBody ReportarDanioRequest request) {
        return reportarDanio.reportar(request.toCommand(id, SolicitanteJwt.desde(jwt)));
    }
}
