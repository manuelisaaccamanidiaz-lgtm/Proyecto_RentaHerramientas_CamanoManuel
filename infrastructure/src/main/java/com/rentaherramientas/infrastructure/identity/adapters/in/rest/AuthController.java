package com.rentaherramientas.infrastructure.identity.adapters.in.rest;

import com.rentaherramientas.application.identity.dto.TokenResponse;
import com.rentaherramientas.application.identity.dto.UsuarioResponse;
import com.rentaherramientas.application.identity.usecase.AutenticarUsuarioUseCase;
import com.rentaherramientas.application.identity.usecase.RegistrarUsuarioUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada público: registro y login. Solo traduce HTTP ↔ command/DTO;
 * las reglas viven en los casos de uso.
 *
 * Nota: `@SecurityRequirements` sin valores anula, solo en Swagger, el requisito global de token definido en
 *   OpenApiConfig: estos endpoints son públicos.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Registro público y login con JWT")
@SecurityRequirements
public class AuthController {

    private final RegistrarUsuarioUseCase registrarUsuario;
    private final AutenticarUsuarioUseCase autenticarUsuario;

    /** Recibe los casos de uso por constructor (inyección de dependencias de Spring). */
    public AuthController(RegistrarUsuarioUseCase registrarUsuario, AutenticarUsuarioUseCase autenticarUsuario) {
        this.registrarUsuario = registrarUsuario;
        this.autenticarUsuario = autenticarUsuario;
    }

    /**
     * POST /api/auth/registro : crea un CLIENTE o un PROVEEDOR y responde 201 Created.
     *
     * Nota: `@Valid @RequestBody`: Spring convierte el JSON en el record y valida sus anotaciones antes de
     *   entrar al método; si falla, responde 400 (ver GlobalExceptionHandler).
     */
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra un CLIENTE o un PROVEEDOR")
    public UsuarioResponse registrar(@Valid @RequestBody RegistroUsuarioRequest request) {
        return registrarUsuario.registrar(request.toCommand());
    }

    /** POST /api/auth/login : valida credenciales y devuelve el token JWT. */
    @PostMapping("/login")
    @Operation(summary = "Inicia sesión y devuelve un JWT Bearer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credenciales válidas: token JWT emitido",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TokenResponse.class),
                            examples = @ExampleObject(name = "Login exitoso", value = """
                                    {
                                      "token": "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJyZW50YS1oZXJyYW1pZW50YXMiLCJzdWIiOiIyMiJ9.4kQnZ8u1",
                                      "tipo": "Bearer",
                                      "expiraEn": "2026-10-19T14:30:00Z",
                                      "rol": "CLIENTE"
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "Email o contraseña incorrectos",
                    content = @Content(mediaType = "application/problem+json",
                            examples = @ExampleObject(name = "Credenciales inválidas", value = """
                                    {
                                      "type": "about:blank",
                                      "title": "No autenticado",
                                      "status": 401,
                                      "detail": "Credenciales inválidas",
                                      "instance": "/api/auth/login"
                                    }
                                    """)))
    })
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return autenticarUsuario.autenticar(request.toCommand());
    }
}
