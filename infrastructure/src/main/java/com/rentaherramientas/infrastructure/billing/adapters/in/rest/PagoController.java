package com.rentaherramientas.infrastructure.billing.adapters.in.rest;

import com.rentaherramientas.application.billing.command.FiltroHistorialPagos;
import com.rentaherramientas.application.billing.dto.PagoResponse;
import com.rentaherramientas.application.billing.usecase.ConfirmarPagoUseCase;
import com.rentaherramientas.application.billing.usecase.ConsultarPagosUseCase;
import com.rentaherramientas.application.billing.usecase.PagarReservaUseCase;
import com.rentaherramientas.application.billing.usecase.RechazarPagoUseCase;
import com.rentaherramientas.application.shared.dto.PaginaResponse;
import com.rentaherramientas.domain.billing.model.valueobject.EstadoPago;
import com.rentaherramientas.domain.billing.model.valueobject.MetodoPago;
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
 * Adaptador de entrada de pagos simulados. SecurityConfig filtra por rol; quién
 * es dueño de qué pago lo deciden los casos de uso.
 */
@RestController
@Tag(name = "Pagos", description = "Pagos simulados de reservas (TARJETA/PSE al instante, EFECTIVO con confirmación)")
public class PagoController {

    private static final String TAMANO_POR_DEFECTO = "20";

    private final PagarReservaUseCase pagarReserva;
    private final ConfirmarPagoUseCase confirmarPago;
    private final RechazarPagoUseCase rechazarPago;
    private final ConsultarPagosUseCase consultarPagos;

    /** Recibe los casos de uso por constructor (inyección de dependencias de Spring). */
    public PagoController(PagarReservaUseCase pagarReserva, ConfirmarPagoUseCase confirmarPago,
                          RechazarPagoUseCase rechazarPago, ConsultarPagosUseCase consultarPagos) {
        this.pagarReserva = pagarReserva;
        this.confirmarPago = confirmarPago;
        this.rechazarPago = rechazarPago;
        this.consultarPagos = consultarPagos;
    }

    /** POST /api/pagos : el cliente paga una reserva ACEPTADA (201 Created). */
    @PostMapping("/api/pagos")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Paga una reserva ACEPTADA propia (solo CLIENTE)",
            description = "El monto lo calcula el servidor (precio total de la reserva). TARJETA y PSE se aprueban "
                    + "al instante y emiten la factura; EFECTIVO queda PENDIENTE hasta que el proveedor confirme.")
    public PagoResponse pagar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PagarReservaRequest request) {
        return pagarReserva.pagar(request.toCommand(SolicitanteJwt.desde(jwt)));
    }

    /** GET /api/pagos/mios : pagos del cliente autenticado. */
    @GetMapping("/api/pagos/mios")
    @Operation(summary = "Pagos del cliente autenticado (solo CLIENTE)")
    public PaginaResponse<PagoResponse> misPagos(@AuthenticationPrincipal Jwt jwt,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = TAMANO_POR_DEFECTO) int size) {
        return consultarPagos.misPagos(Long.valueOf(jwt.getSubject()), page, size);
    }

    /** GET /api/pagos/{id} : detalle de un pago. */
    @GetMapping("/api/pagos/{id}")
    @Operation(summary = "Detalle de un pago (cliente, proveedor de la herramienta o ADMIN)")
    public PagoResponse porId(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return consultarPagos.porId(id, SolicitanteJwt.desde(jwt));
    }

    /** PATCH /api/pagos/{id}/confirmar : aprueba un pago PENDIENTE y emite su factura. */
    @PatchMapping("/api/pagos/{id}/confirmar")
    @Operation(summary = "Confirma un pago PENDIENTE y emite la factura (proveedor dueño o ADMIN)")
    public PagoResponse confirmar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return confirmarPago.confirmar(id, SolicitanteJwt.desde(jwt));
    }

    /** PATCH /api/pagos/{id}/rechazar : rechaza un pago PENDIENTE. */
    @PatchMapping("/api/pagos/{id}/rechazar")
    @Operation(summary = "Rechaza un pago PENDIENTE (proveedor dueño o ADMIN)")
    public PagoResponse rechazar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return rechazarPago.rechazar(id, SolicitanteJwt.desde(jwt));
    }

    /** GET /api/proveedor/pagos : pagos de las herramientas del proveedor autenticado. */
    @GetMapping("/api/proveedor/pagos")
    @Operation(summary = "Pagos de reservas de las herramientas del proveedor autenticado (solo PROVEEDOR)")
    public PaginaResponse<PagoResponse> delProveedor(@AuthenticationPrincipal Jwt jwt,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = TAMANO_POR_DEFECTO) int size) {
        return consultarPagos.delProveedor(Long.valueOf(jwt.getSubject()), page, size);
    }

    /** GET /api/admin/pagos : historial de pagos con filtros (solo ADMIN). */
    @GetMapping("/api/admin/pagos")
    @Operation(summary = "Historial de pagos con filtros (solo ADMIN)",
            description = "desde/hasta filtran por la fecha del pago (ambos días incluidos).")
    public PaginaResponse<PagoResponse> historial(
            @RequestParam(required = false) EstadoPago estado,
            @RequestParam(required = false) MetodoPago metodo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = TAMANO_POR_DEFECTO) int size) {
        return consultarPagos.historial(new FiltroHistorialPagos(estado, metodo, desde, hasta, page, size));
    }
}
