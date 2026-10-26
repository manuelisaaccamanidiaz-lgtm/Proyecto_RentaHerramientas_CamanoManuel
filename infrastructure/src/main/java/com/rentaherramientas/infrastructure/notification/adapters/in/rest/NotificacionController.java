package com.rentaherramientas.infrastructure.notification.adapters.in.rest;

import com.rentaherramientas.application.notification.dto.NotificacionResponse;
import com.rentaherramientas.application.notification.usecase.GestionarNotificacionesUseCase;
import com.rentaherramientas.application.shared.dto.PaginaResponse;
import com.rentaherramientas.infrastructure.shared.adapters.in.rest.SolicitanteJwt;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada de notificaciones. Cualquier usuario autenticado
 * (SecurityConfig: anyRequest().authenticated()) y siempre sobre SUS avisos: el
 * dueño sale del JWT, nunca de la URL ni del cuerpo.
 */
@RestController
@RequestMapping("/api/notificaciones")
@Tag(name = "Notificaciones", description = "Avisos dentro de la app para el usuario autenticado")
public class NotificacionController {

    private static final String TAMANO_POR_DEFECTO = "20";

    private final GestionarNotificacionesUseCase notificaciones;

    /** Recibe el caso de uso por constructor (inyección de dependencias de Spring). */
    public NotificacionController(GestionarNotificacionesUseCase notificaciones) {
        this.notificaciones = notificaciones;
    }

    /** GET /api/notificaciones/mias : notificaciones del usuario, de la más reciente a la más antigua. */
    @GetMapping("/mias")
    @Operation(summary = "Mis notificaciones (paginado); soloNoLeidas=true filtra las pendientes")
    public PaginaResponse<NotificacionResponse> mias(@AuthenticationPrincipal Jwt jwt,
                                                     @RequestParam(defaultValue = "false") boolean soloNoLeidas,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = TAMANO_POR_DEFECTO) int size) {
        return notificaciones.mias(SolicitanteJwt.desde(jwt), soloNoLeidas, page, size);
    }

    /** GET /api/notificaciones/mias/conteo : {"noLeidas": n}, para el badge de la campana. */
    @GetMapping("/mias/conteo")
    @Operation(summary = "Cuántas notificaciones sin leer tengo")
    public ConteoNoLeidasResponse conteo(@AuthenticationPrincipal Jwt jwt) {
        return new ConteoNoLeidasResponse(notificaciones.contarNoLeidas(SolicitanteJwt.desde(jwt)));
    }

    /** PATCH /api/notificaciones/{id}/leida : 204; 404 si no existe o es de otro usuario. */
    @PatchMapping("/{id}/leida")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Marca una de mis notificaciones como leída (404 si no es mía)")
    public void marcarLeida(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        notificaciones.marcarLeida(id, SolicitanteJwt.desde(jwt));
    }

    /** PATCH /api/notificaciones/mias/leidas : marca todas como leídas (204). */
    @PatchMapping("/mias/leidas")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Marca todas mis notificaciones como leídas")
    public void marcarTodasLeidas(@AuthenticationPrincipal Jwt jwt) {
        notificaciones.marcarTodasLeidas(SolicitanteJwt.desde(jwt));
    }

    /** Cuerpo de GET /mias/conteo. */
    public record ConteoNoLeidasResponse(long noLeidas) {
    }
}
