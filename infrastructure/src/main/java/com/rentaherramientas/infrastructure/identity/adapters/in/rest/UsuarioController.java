package com.rentaherramientas.infrastructure.identity.adapters.in.rest;

import com.rentaherramientas.application.identity.dto.UsuarioResponse;
import com.rentaherramientas.application.identity.usecase.ActualizarUsuarioUseCase;
import com.rentaherramientas.application.identity.usecase.ConsultarUsuarioUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    public UsuarioController(ConsultarUsuarioUseCase consultarUsuario, ActualizarUsuarioUseCase actualizarUsuario) {
        this.consultarUsuario = consultarUsuario;
        this.actualizarUsuario = actualizarUsuario;
    }

    @GetMapping
    @Operation(summary = "Datos del usuario autenticado")
    public UsuarioResponse me(@AuthenticationPrincipal Jwt jwt) {
        return consultarUsuario.buscarPorId(Long.valueOf(jwt.getSubject()));
    }

    @PutMapping
    @Operation(summary = "Actualiza el nombre y los datos de rol del usuario autenticado")
    public UsuarioResponse actualizar(@AuthenticationPrincipal Jwt jwt,
                                      @Valid @RequestBody ActualizarUsuarioRequest request) {
        return actualizarUsuario.actualizar(request.toCommand(Long.valueOf(jwt.getSubject())));
    }
}
