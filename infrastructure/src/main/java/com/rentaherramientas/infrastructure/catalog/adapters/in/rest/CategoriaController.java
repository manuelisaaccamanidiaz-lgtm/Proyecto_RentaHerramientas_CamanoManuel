package com.rentaherramientas.infrastructure.catalog.adapters.in.rest;

import com.rentaherramientas.application.catalog.dto.CategoriaResponse;
import com.rentaherramientas.application.catalog.usecase.ActualizarCategoriaUseCase;
import com.rentaherramientas.application.catalog.usecase.ConsultarCategoriaUseCase;
import com.rentaherramientas.application.catalog.usecase.CrearCategoriaUseCase;
import com.rentaherramientas.application.catalog.usecase.EliminarCategoriaUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Adaptador de entrada de categorías. Lectura pública; escritura solo ADMIN
 * (restringida en SecurityConfig).
 */
@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorías", description = "Categorías del catálogo de herramientas")
public class CategoriaController {

    private final CrearCategoriaUseCase crearCategoria;
    private final ActualizarCategoriaUseCase actualizarCategoria;
    private final EliminarCategoriaUseCase eliminarCategoria;
    private final ConsultarCategoriaUseCase consultarCategoria;

    /** Recibe los casos de uso por constructor (inyección de dependencias de Spring). */
    public CategoriaController(CrearCategoriaUseCase crearCategoria, ActualizarCategoriaUseCase actualizarCategoria,
                               EliminarCategoriaUseCase eliminarCategoria,
                               ConsultarCategoriaUseCase consultarCategoria) {
        this.crearCategoria = crearCategoria;
        this.actualizarCategoria = actualizarCategoria;
        this.eliminarCategoria = eliminarCategoria;
        this.consultarCategoria = consultarCategoria;
    }

    /** GET /api/categorias : todas las categorías, ordenadas por nombre. */
    @GetMapping
    @SecurityRequirements
    @Operation(summary = "Lista todas las categorías (público)")
    public List<CategoriaResponse> listar() {
        return consultarCategoria.listar();
    }

    /** GET /api/categorias/{id} : detalle de una categoría. */
    @GetMapping("/{id}")
    @SecurityRequirements
    @Operation(summary = "Detalle de una categoría (público)")
    public CategoriaResponse buscarPorId(@PathVariable Long id) {
        return consultarCategoria.buscarPorId(id);
    }

    /** POST /api/categorias : crea una categoría y responde 201 Created. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea una categoría (solo ADMIN)")
    public CategoriaResponse crear(@Valid @RequestBody CategoriaRequest request) {
        return crearCategoria.crear(request.toCommand());
    }

    /** PUT /api/categorias/{id} : actualiza nombre y descripción. */
    @PutMapping("/{id}")
    @Operation(summary = "Actualiza una categoría (solo ADMIN)")
    public CategoriaResponse actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
        return actualizarCategoria.actualizar(id, request.toCommand());
    }

    /** DELETE /api/categorias/{id} : elimina la categoría y responde 204 No Content. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina una categoría sin herramientas (solo ADMIN)",
            description = "Si alguna herramienta tiene asignada la categoría, responde 409 y no la borra.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoría eliminada", content = @Content),
            @ApiResponse(responseCode = "403", description = "El usuario no es ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "La categoría no existe", content = @Content),
            @ApiResponse(responseCode = "409", description = "La categoría está en uso por alguna herramienta",
                    content = @Content(mediaType = "application/problem+json",
                            examples = @ExampleObject(name = "Categoría en uso", value = """
                                    {
                                      "type": "about:blank",
                                      "title": "Conflicto",
                                      "status": 409,
                                      "detail": "No se puede eliminar la categoría Corte porque tiene herramientas asociadas; quítala de esas herramientas primero",
                                      "instance": "/api/categorias/2"
                                    }
                                    """)))
    })
    public void eliminar(@PathVariable Long id) {
        eliminarCategoria.eliminar(id);
    }
}
