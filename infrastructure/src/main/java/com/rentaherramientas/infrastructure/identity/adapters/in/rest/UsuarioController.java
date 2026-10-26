package com.rentaherramientas.infrastructure.identity.adapters.in.rest;

import com.rentaherramientas.application.identity.dto.UsuarioResponse;
import com.rentaherramientas.application.identity.usecase.ActualizarUsuarioUseCase;
import com.rentaherramientas.application.identity.usecase.CambiarPasswordUseCase;
import com.rentaherramientas.application.identity.usecase.ConsultarUsuarioUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada del perfil propio. El id del usuario autenticado sale del
 * claim "sub" del JWT ya validado; nunca del cliente. La administración de
 * usuarios vive en AdminUsuarioController.
 */
@RestController
@RequestMapping("/api/usuarios/me")
@Tag(name = "Usuarios", description = "Perfil del usuario autenticado")
public class UsuarioController {

    private final ConsultarUsuarioUseCase consultarUsuario;
    private final ActualizarUsuarioUseCase actualizarUsuario;
    private final CambiarPasswordUseCase cambiarPassword;

    /** Recibe los casos de uso por constructor (inyección de dependencias de Spring). */
    public UsuarioController(ConsultarUsuarioUseCase consultarUsuario, ActualizarUsuarioUseCase actualizarUsuario,
                             CambiarPasswordUseCase cambiarPassword) {
        this.consultarUsuario = consultarUsuario;
        this.actualizarUsuario = actualizarUsuario;
        this.cambiarPassword = cambiarPassword;
    }

    /** GET /api/usuarios/me : datos del usuario autenticado (id tomado del claim "sub"). */
    @GetMapping
    @Operation(summary = "Datos del usuario autenticado")
    public UsuarioResponse me(@AuthenticationPrincipal Jwt jwt) {
        return consultarUsuario.buscarPorId(Long.valueOf(jwt.getSubject()));
    }

    /** PUT /api/usuarios/me : el usuario actualiza su propio perfil. */
    @PutMapping
    @Operation(summary = "Actualiza el nombre y los datos de rol del usuario autenticado")
    public UsuarioResponse actualizar(@AuthenticationPrincipal Jwt jwt,
                                      @Valid @RequestBody ActualizarUsuarioRequest request) {
        return actualizarUsuario.actualizar(request.toCommand(Long.valueOf(jwt.getSubject())));
    }

    /** PATCH /api/usuarios/me/password : el usuario cambia su contraseña y responde 204 No Content. */
    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cambia la contraseña del usuario autenticado (exige la contraseña actual)")
    public void cambiarPassword(@AuthenticationPrincipal Jwt jwt,
                                @Valid @RequestBody CambiarPasswordRequest request) {
        cambiarPassword.cambiarPassword(request.toCommand(Long.valueOf(jwt.getSubject())));
    }
}
