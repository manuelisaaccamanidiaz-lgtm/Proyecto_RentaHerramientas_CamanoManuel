package com.rentaherramientas.infrastructure.reporting.adapters.in.rest;

import com.rentaherramientas.application.reporting.dto.ClienteFrecuenteResponse;
import com.rentaherramientas.application.reporting.dto.HerramientaAlquiladaResponse;
import com.rentaherramientas.application.reporting.dto.IngresoMensualResponse;
import com.rentaherramientas.application.reporting.dto.InventarioResponse;
import com.rentaherramientas.application.reporting.dto.RentabilidadResponse;
import com.rentaherramientas.application.reporting.dto.ResumenGeneralResponse;
import com.rentaherramientas.application.reporting.dto.ResumenProveedorResponse;
import com.rentaherramientas.application.reporting.usecase.ConsultarReportesUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Adaptador de entrada de reportes (solo lectura). /api/admin/** es solo ADMIN y
 * /api/proveedor/** solo PROVEEDOR (SecurityConfig); el proveedor siempre ve
 * únicamente sus herramientas porque su id sale del JWT.
 */
@RestController
@Tag(name = "Reportes", description = "Indicadores agregados de la plataforma y del proveedor")
public class ReporteController {

    private static final String LIMITE_POR_DEFECTO = "10";

    private final ConsultarReportesUseCase consultarReportes;

    public ReporteController(ConsultarReportesUseCase consultarReportes) {
        this.consultarReportes = consultarReportes;
    }

    @GetMapping("/api/admin/reportes/resumen")
    @Operation(summary = "Totales: ingresos, reservas por estado, usuarios por rol y herramientas por estado (ADMIN)")
    public ResumenGeneralResponse resumen() {
        return consultarReportes.resumenGeneral();
    }

    @GetMapping("/api/admin/reportes/ingresos")
    @Operation(summary = "Ingresos (pagos aprobados) por mes (ADMIN)",
            description = "Por defecto los últimos 12 meses; periodo máximo 5 años.")
    public List<IngresoMensualResponse> ingresos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return consultarReportes.ingresosPorMes(desde, hasta);
    }

    @GetMapping("/api/admin/reportes/herramientas-mas-alquiladas")
    @Operation(summary = "Ranking de herramientas por número de alquileres (ADMIN; limite 1-50)")
    public List<HerramientaAlquiladaResponse> herramientasMasAlquiladas(
            @RequestParam(defaultValue = LIMITE_POR_DEFECTO) int limite) {
        return consultarReportes.herramientasMasAlquiladas(limite);
    }

    @GetMapping("/api/admin/reportes/clientes-frecuentes")
    @Operation(summary = "Ranking de clientes por número de alquileres (ADMIN; limite 1-50)")
    public List<ClienteFrecuenteResponse> clientesFrecuentes(
            @RequestParam(defaultValue = LIMITE_POR_DEFECTO) int limite) {
        return consultarReportes.clientesFrecuentes(limite);
    }

    @GetMapping("/api/admin/reportes/inventario")
    @Operation(summary = "Herramientas por estado y por categoría (ADMIN)")
    public InventarioResponse inventario() {
        return consultarReportes.inventario();
    }

    @GetMapping("/api/admin/reportes/rentabilidad")
    @Operation(summary = "Ingresos por herramienta y por proveedor (ADMIN; limite 1-50)")
    public RentabilidadResponse rentabilidad(@RequestParam(defaultValue = LIMITE_POR_DEFECTO) int limite) {
        return consultarReportes.rentabilidad(limite);
    }

    @GetMapping("/api/proveedor/reportes/resumen")
    @Operation(summary = "Ingresos, reservas por estado y herramientas por estado de SUS herramientas (PROVEEDOR)")
    public ResumenProveedorResponse resumenProveedor(@AuthenticationPrincipal Jwt jwt) {
        return consultarReportes.resumenProveedor(Long.valueOf(jwt.getSubject()));
    }
}
