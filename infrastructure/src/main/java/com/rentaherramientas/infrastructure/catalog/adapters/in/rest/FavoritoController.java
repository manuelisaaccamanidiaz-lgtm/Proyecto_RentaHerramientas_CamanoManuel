package com.rentaherramientas.infrastructure.catalog.adapters.in.rest;

import com.rentaherramientas.application.catalog.dto.HerramientaResponse;
import com.rentaherramientas.application.catalog.usecase.GestionarFavoritosUseCase;
import com.rentaherramientas.application.shared.dto.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada de favoritos (solo CLIENTE, ver SecurityConfig). El
 * cliente sale del JWT, nunca de la URL ni del cuerpo.
 *
 * Por qué PUT/DELETE sobre /api/favoritos/{herramientaId} y no POST:
 *   - La URL identifica el recurso completo ("el favorito H del cliente autenticado"), así que
 *     marcar es "deja este recurso en estado existe" (PUT) y desmarcar, "deja de existir" (DELETE).
 *   - HTTP define PUT y DELETE como idempotentes: N peticiones iguales dejan el servidor como una
 *     sola. POST no lo es (cada POST suele crear algo nuevo, p. ej. otra fila o un 409 al repetir),
 *     de modo que un reintento automático o un doble clic podría duplicar o fallar.
 *   - Por eso ambos responden siempre 204: repetir no es un error ni cambia el resultado.
 */
@RestController
@RequestMapping("/api/favoritos")
@Tag(name = "Favoritos", description = "Herramientas favoritas del cliente autenticado")
public class FavoritoController {

    private static final String TAMANO_POR_DEFECTO = "20";

    private final GestionarFavoritosUseCase gestionarFavoritos;

    /** Recibe el caso de uso por constructor (inyección de dependencias de Spring). */
    public FavoritoController(GestionarFavoritosUseCase gestionarFavoritos) {
        this.gestionarFavoritos = gestionarFavoritos;
    }

    /** PUT /api/favoritos/{herramientaId} : marca la herramienta (idempotente), responde 204. */
    @PutMapping("/{herramientaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Marca una herramienta como favorita (idempotente; 404 si no existe, 409 si está inactiva)")
    public void marcar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long herramientaId) {
        gestionarFavoritos.marcar(Long.valueOf(jwt.getSubject()), herramientaId);
    }

    /** DELETE /api/favoritos/{herramientaId} : desmarca la herramienta (idempotente), responde 204. */
    @DeleteMapping("/{herramientaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Quita una herramienta de favoritos (idempotente: 204 aunque no estuviera)")
    public void desmarcar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long herramientaId) {
        gestionarFavoritos.desmarcar(Long.valueOf(jwt.getSubject()), herramientaId);
    }

    /** GET /api/favoritos/mios : herramientas favoritas del cliente, la más reciente primero. */
    @GetMapping("/mios")
    @Operation(summary = "Herramientas favoritas del cliente autenticado (paginado, size máx. 50)")
    public PaginaResponse<HerramientaResponse> misFavoritos(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = TAMANO_POR_DEFECTO) int size) {
        return gestionarFavoritos.misFavoritos(Long.valueOf(jwt.getSubject()), page, size);
    }
}
