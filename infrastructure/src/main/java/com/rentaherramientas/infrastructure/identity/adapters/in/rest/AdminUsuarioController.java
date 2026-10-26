package com.rentaherramientas.infrastructure.identity.adapters.in.rest;

import com.rentaherramientas.application.identity.dto.UsuarioResponse;
import com.rentaherramientas.application.identity.usecase.ActualizarUsuarioUseCase;
import com.rentaherramientas.application.identity.usecase.CambiarEstadoUsuarioUseCase;
import com.rentaherramientas.application.identity.usecase.CambiarRolUsuarioUseCase;
import com.rentaherramientas.application.identity.usecase.ConsultarUsuarioUseCase;
import com.rentaherramientas.application.shared.dto.PaginaResponse;
import com.rentaherramientas.domain.identity.model.valueobject.Rol;
import com.rentaherramientas.infrastructure.shared.adapters.in.rest.SolicitanteJwt;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    private final CambiarRolUsuarioUseCase cambiarRolUsuario;

    /** Recibe los casos de uso por constructor (inyección de dependencias de Spring). */
    public AdminUsuarioController(ConsultarUsuarioUseCase consultarUsuario, ActualizarUsuarioUseCase actualizarUsuario,
                                  CambiarEstadoUsuarioUseCase cambiarEstadoUsuario,
                                  CambiarRolUsuarioUseCase cambiarRolUsuario) {
        this.consultarUsuario = consultarUsuario;
        this.actualizarUsuario = actualizarUsuario;
        this.cambiarEstadoUsuario = cambiarEstadoUsuario;
        this.cambiarRolUsuario = cambiarRolUsuario;
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

    /** PATCH /api/admin/usuarios/{id}/rol : cambia el rol y los datos de subtipo de un usuario. */
    @PatchMapping("/{id}/rol")
    @Operation(summary = "Cambia el rol de un usuario (CLIENTE, PROVEEDOR o ADMIN)",
            description = "PROVEEDOR exige datosProveedor y CLIENTE exige datosCliente; ADMIN no admite ninguno. "
                    + "Los datos del rol anterior se eliminan. No se permite si el usuario tiene reservas (como "
                    + "cliente) o herramientas (como proveedor). El rol viaja en el JWT: el usuario debe volver "
                    + "a iniciar sesión para que el cambio surta efecto.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rol cambiado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioResponse.class),
                            examples = @ExampleObject(name = "Cliente convertido en proveedor", value = """
                                    {
                                      "id": 22,
                                      "nombre": "Andrés Felipe Rueda Pinzón",
                                      "email": "andres.rueda@gmail.com",
                                      "rol": "PROVEEDOR",
                                      "activo": true,
                                      "datosProveedor": {
                                        "razonSocial": "Herramientas Rueda SAS",
                                        "nit": "901234567-8"
                                      },
                                      "datosCliente": null
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "El administrador intenta cambiar su propio rol",
                    content = @Content(mediaType = "application/problem+json",
                            examples = @ExampleObject(name = "Cambio del propio rol", value = """
                                    {
                                      "type": "about:blank",
                                      "title": "Acceso denegado",
                                      "status": 403,
                                      "detail": "Un administrador no puede cambiar su propio rol",
                                      "instance": "/api/admin/usuarios/1/rol"
                                    }
                                    """))),
            @ApiResponse(responseCode = "409", description = "El usuario tiene historial o ya tiene ese rol",
                    content = @Content(mediaType = "application/problem+json",
                            examples = {
                                    @ExampleObject(name = "Usuario con historial", value = """
                                            {
                                              "type": "about:blank",
                                              "title": "Conflicto",
                                              "status": 409,
                                              "detail": "El usuario tiene reservas registradas: no se puede cambiar su rol",
                                              "instance": "/api/admin/usuarios/22/rol"
                                            }
                                            """),
                                    @ExampleObject(name = "Mismo rol", value = """
                                            {
                                              "type": "about:blank",
                                              "title": "Regla de negocio incumplida",
                                              "status": 409,
                                              "detail": "El usuario 22 ya tiene el rol CLIENTE",
                                              "instance": "/api/admin/usuarios/22/rol"
                                            }
                                            """)
                            }))
    })
    public UsuarioResponse cambiarRol(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                      @Valid @RequestBody CambiarRolRequest request) {
        return cambiarRolUsuario.cambiarRol(request.toCommand(id, SolicitanteJwt.desde(jwt)));
    }
}
