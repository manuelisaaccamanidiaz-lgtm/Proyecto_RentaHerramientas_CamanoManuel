package com.rentaherramientas.infrastructure.identity.adapters.in.rest;

import com.rentaherramientas.application.identity.dto.UsuarioResponse;
import com.rentaherramientas.application.identity.usecase.ActualizarUsuarioUseCase;
import com.rentaherramientas.application.identity.usecase.CambiarEstadoUsuarioUseCase;
import com.rentaherramientas.application.identity.usecase.ConsultarUsuarioUseCase;
import com.rentaherramientas.application.shared.dto.PaginaResponse;
import com.rentaherramientas.domain.identity.model.valueobject.Rol;
import com.rentaherramientas.infrastructure.shared.adapters.in.rest.SolicitanteJwt;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada para la gestión de usuarios por el ADMIN. SecurityConfig
 * restringe /api/admin/** a ROLE_ADMIN. "Eliminar" un usuario es desactivarlo.
 *
 * TÉCNICA AVANZADA (springdoc): `@Tag` y `@Operation`
 *   Qué hace: no cambian el comportamiento; agrupan y describen los endpoints en Swagger UI.
 */
@RestController
@RequestMapping("/api/admin/usuarios")
@Tag(name = "Administración de usuarios", description = "Gestión de proveedores y clientes (solo ADMIN)")
public class AdminUsuarioController {

    private final ConsultarUsuarioUseCase consultarUsuario;
    private final ActualizarUsuarioUseCase actualizarUsuario;
    private final CambiarEstadoUsuarioUseCase cambiarEstadoUsuario;

    /** Recibe los casos de uso por constructor (inyección de dependencias de Spring). */
    public AdminUsuarioController(ConsultarUsuarioUseCase consultarUsuario, ActualizarUsuarioUseCase actualizarUsuario,
                                  CambiarEstadoUsuarioUseCase cambiarEstadoUsuario) {
        this.consultarUsuario = consultarUsuario;
        this.actualizarUsuario = actualizarUsuario;
        this.cambiarEstadoUsuario = cambiarEstadoUsuario;
    }

    /**
     * GET /api/admin/usuarios?rol=&page=&size= : lista paginada de usuarios.
     *
     * Nota: `@RequestParam(required = false) Rol rol`: Spring convierte el texto del query string ("CLIENTE")
     *   en la constante del enum; si falta, llega null.
     */
    @GetMapping
    @Operation(summary = "Lista paginada de usuarios, opcionalmente por rol (size máx. 50)")
    public PaginaResponse<UsuarioResponse> listar(@RequestParam(required = false) Rol rol,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return consultarUsuario.listar(rol, page, size);
    }

    /** GET /api/admin/usuarios/{id} : detalle de un usuario. */
    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un usuario")
    public UsuarioResponse buscarPorId(@PathVariable Long id) {
        return consultarUsuario.buscarPorId(id);
    }

    /** PUT /api/admin/usuarios/{id} : el ADMIN actualiza los datos de cualquier usuario. */
    @PutMapping("/{id}")
    @Operation(summary = "Actualiza nombre y datos de proveedor/cliente según su rol")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarUsuarioRequest request) {
        return actualizarUsuario.actualizar(request.toCommand(id));
    }

    /**
     * PATCH /api/admin/usuarios/{id}/desactivar : borrado lógico del usuario.
     *
     * Nota: `@AuthenticationPrincipal Jwt jwt`: Spring Security inyecta el token ya validado de la petición,
     *   del que se obtiene quién es el administrador.
     */
    @PatchMapping("/{id}/desactivar")
    @Operation(summary = "Desactiva (borrado lógico) un usuario; un ADMIN no puede desactivarse a sí mismo")
    public UsuarioResponse desactivar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return cambiarEstadoUsuario.desactivar(id, SolicitanteJwt.desde(jwt));
    }

    /** PATCH /api/admin/usuarios/{id}/activar : reactiva un usuario. */
    @PatchMapping("/{id}/activar")
    @Operation(summary = "Reactiva un usuario")
    public UsuarioResponse activar(@PathVariable Long id) {
        return cambiarEstadoUsuario.activar(id);
    }
}
