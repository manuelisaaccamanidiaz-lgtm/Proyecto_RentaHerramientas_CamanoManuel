package com.rentaherramientas.infrastructure.identity.adapters.in.rest;

import com.rentaherramientas.application.identity.dto.UsuarioResponse;
import com.rentaherramientas.application.identity.usecase.ConsultarUsuarioUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Adaptador de entrada para consultar usuarios. El id del usuario autenticado
 * sale del claim "sub" del JWT ya validado; nunca del cliente. El acceso a
 * /api/admin/** lo restringe SecurityConfig a ROLE_ADMIN.
 */
@RestController
@Tag(name = "Usuarios", description = "Perfil propio y administración de usuarios")
public class UsuarioController {

    private final ConsultarUsuarioUseCase consultarUsuario;

    public UsuarioController(ConsultarUsuarioUseCase consultarUsuario) {
        this.consultarUsuario = consultarUsuario;
    }

    @GetMapping("/api/usuarios/me")
    @Operation(summary = "Datos del usuario autenticado")
    public UsuarioResponse me(@AuthenticationPrincipal Jwt jwt) {
        return consultarUsuario.buscarPorId(Long.valueOf(jwt.getSubject()));
    }

    @GetMapping("/api/admin/usuarios")
    @Operation(summary = "Lista todos los usuarios (solo ADMIN)")
    public List<UsuarioResponse> listar() {
        return consultarUsuario.listarTodos();
    }
}
