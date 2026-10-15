package com.rentaherramientas.infrastructure.billing.adapters.in.rest;

import com.rentaherramientas.application.billing.dto.ArchivoPdf;
import com.rentaherramientas.application.billing.dto.FacturaResponse;
import com.rentaherramientas.application.billing.usecase.ConsultarFacturasUseCase;
import com.rentaherramientas.application.shared.dto.PaginaResponse;
import com.rentaherramientas.infrastructure.shared.adapters.in.rest.SolicitanteJwt;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada de facturas: consultas por actor y descarga en PDF.
 */
@RestController
@Tag(name = "Facturas", description = "Facturas emitidas al aprobarse un pago y su descarga en PDF")
public class FacturaController {

    private static final String TAMANO_POR_DEFECTO = "20";

    private final ConsultarFacturasUseCase consultarFacturas;

    public FacturaController(ConsultarFacturasUseCase consultarFacturas) {
        this.consultarFacturas = consultarFacturas;
    }

    @GetMapping("/api/facturas/mias")
    @Operation(summary = "Facturas del cliente autenticado (solo CLIENTE)")
    public PaginaResponse<FacturaResponse> misFacturas(@AuthenticationPrincipal Jwt jwt,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = TAMANO_POR_DEFECTO) int size) {
        return consultarFacturas.misFacturas(Long.valueOf(jwt.getSubject()), page, size);
    }

    @GetMapping("/api/facturas/{id}")
    @Operation(summary = "Detalle de una factura (cliente, proveedor de la herramienta o ADMIN)")
    public FacturaResponse porId(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return consultarFacturas.porId(id, SolicitanteJwt.desde(jwt));
    }

    @GetMapping(value = "/api/facturas/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Descarga la factura en PDF (cliente, proveedor de la herramienta o ADMIN)")
    public ResponseEntity<byte[]> pdf(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        ArchivoPdf archivo = consultarFacturas.pdf(id, SolicitanteJwt.desde(jwt));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(archivo.nombreArchivo()).build().toString())
                .body(archivo.contenido());
    }

    @GetMapping("/api/proveedor/facturas")
    @Operation(summary = "Facturas de reservas de las herramientas del proveedor autenticado (solo PROVEEDOR)")
    public PaginaResponse<FacturaResponse> delProveedor(@AuthenticationPrincipal Jwt jwt,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = TAMANO_POR_DEFECTO) int size) {
        return consultarFacturas.delProveedor(Long.valueOf(jwt.getSubject()), page, size);
    }

    @GetMapping("/api/admin/facturas")
    @Operation(summary = "Todas las facturas (solo ADMIN)")
    public PaginaResponse<FacturaResponse> todas(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = TAMANO_POR_DEFECTO) int size) {
        return consultarFacturas.todas(page, size);
    }
}
