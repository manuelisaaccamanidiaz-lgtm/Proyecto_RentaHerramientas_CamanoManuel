package com.rentaherramientas.infrastructure.catalog.adapters.in.rest;

import com.rentaherramientas.application.catalog.command.CambiarEstadoHerramientaCommand;
import com.rentaherramientas.application.catalog.command.FiltroHerramientas;
import com.rentaherramientas.application.catalog.dto.HerramientaResponse;
import com.rentaherramientas.application.catalog.usecase.ActualizarHerramientaUseCase;
import com.rentaherramientas.application.catalog.usecase.CambiarEstadoHerramientaUseCase;
import com.rentaherramientas.application.catalog.usecase.ConsultarHerramientaUseCase;
import com.rentaherramientas.application.catalog.usecase.RegistrarHerramientaUseCase;
import com.rentaherramientas.application.shared.dto.PaginaResponse;
import com.rentaherramientas.domain.catalog.model.valueobject.EstadoHerramienta;
import com.rentaherramientas.infrastructure.shared.adapters.in.rest.SolicitanteJwt;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada de herramientas: catálogo público, alta y gestión por el
 * proveedor (o un ADMIN) y el panel de inventario del proveedor. El proveedor y
 * el rol salen del JWT; la regla de propiedad la aplica cada caso de uso.
 *
 * Nota: No tiene `@RequestMapping` en la clase porque mezcla rutas de /api/herramientas y /api/proveedor;
 *   cada método declara su ruta completa.
 */
@RestController
@Tag(name = "Herramientas", description = "Catálogo de herramientas e inventario del proveedor")
public class HerramientaController {

    private static final String TAMANO_POR_DEFECTO = "20";

    private final RegistrarHerramientaUseCase registrarHerramienta;
    private final ActualizarHerramientaUseCase actualizarHerramienta;
    private final CambiarEstadoHerramientaUseCase cambiarEstadoHerramienta;
    private final ConsultarHerramientaUseCase consultarHerramienta;

    /** Recibe los casos de uso por constructor (inyección de dependencias de Spring). */
    public HerramientaController(RegistrarHerramientaUseCase registrarHerramienta,
                                 ActualizarHerramientaUseCase actualizarHerramienta,
                                 CambiarEstadoHerramientaUseCase cambiarEstadoHerramienta,
                                 ConsultarHerramientaUseCase consultarHerramienta) {
        this.registrarHerramienta = registrarHerramienta;
        this.actualizarHerramienta = actualizarHerramienta;
        this.cambiarEstadoHerramienta = cambiarEstadoHerramienta;
        this.consultarHerramienta = consultarHerramienta;
    }

    /**
     * GET /api/herramientas : catálogo público con filtros opcionales; oculta las INACTIVAS si no se filtra por estado.
     */
    @GetMapping("/api/herramientas")
    @SecurityRequirements
    @Operation(summary = "Catálogo público con filtros opcionales y paginación (size máx. 50)",
            description = "Sin filtro de estado se ocultan las herramientas INACTIVAS (dadas de baja).")
    public PaginaResponse<HerramientaResponse> buscar(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) EstadoHerramienta estado,
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Long proveedorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = TAMANO_POR_DEFECTO) int size) {
        return consultarHerramienta.buscar(
                new FiltroHerramientas(categoriaId, estado, texto, proveedorId, true, page, size));
    }

    /** GET /api/herramientas/{id} : detalle público de una herramienta. */
    @GetMapping("/api/herramientas/{id}")
    @SecurityRequirements
    @Operation(summary = "Detalle de una herramienta (público)")
    public HerramientaResponse buscarPorId(@PathVariable Long id) {
        return consultarHerramienta.buscarPorId(id);
    }

    /** GET /api/proveedor/herramientas : inventario del proveedor autenticado, incluidas las inactivas. */
    @GetMapping("/api/proveedor/herramientas")
    @Operation(summary = "Inventario del proveedor autenticado, incluidas las inactivas (solo PROVEEDOR)")
    public PaginaResponse<HerramientaResponse> inventario(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) EstadoHerramienta estado,
            @RequestParam(required = false) String texto,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = TAMANO_POR_DEFECTO) int size) {
        Long proveedorId = Long.valueOf(jwt.getSubject());
        return consultarHerramienta.buscar(
                new FiltroHerramientas(categoriaId, estado, texto, proveedorId, false, page, size));
    }

    /** POST /api/herramientas : registra una herramienta del proveedor autenticado (201 Created). */
    @PostMapping("/api/herramientas")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra una herramienta a nombre del proveedor autenticado (solo PROVEEDOR)")
    public HerramientaResponse registrar(@AuthenticationPrincipal Jwt jwt,
                                         @Valid @RequestBody HerramientaRequest request) {
        return registrarHerramienta.registrar(request.toRegistrarCommand(SolicitanteJwt.desde(jwt)));
    }

    /** PUT /api/herramientas/{id} : actualiza datos, tarifa y categorías. */
    @PutMapping("/api/herramientas/{id}")
    @Operation(summary = "Actualiza datos, tarifa y categorías (dueño o ADMIN)")
    public HerramientaResponse actualizar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                          @Valid @RequestBody HerramientaRequest request) {
        return actualizarHerramienta.actualizar(request.toActualizarCommand(id, SolicitanteJwt.desde(jwt)));
    }

    /** PATCH /api/herramientas/{id}/estado : cambia el estado operativo. */
    @PatchMapping("/api/herramientas/{id}/estado")
    @Operation(summary = "Cambia el estado operativo (dueño o ADMIN)")
    public HerramientaResponse cambiarEstado(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                             @Valid @RequestBody CambiarEstadoRequest request) {
        return cambiarEstadoHerramienta.cambiarEstado(
                new CambiarEstadoHerramientaCommand(id, SolicitanteJwt.desde(jwt), request.estado()));
    }

    /** DELETE /api/herramientas/{id} : borrado lógico (inactiva la herramienta), responde 204. */
    @DeleteMapping("/api/herramientas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Da de baja (inactiva) una herramienta (dueño o ADMIN)")
    public void eliminar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        cambiarEstadoHerramienta.eliminar(id, SolicitanteJwt.desde(jwt));
    }
}
