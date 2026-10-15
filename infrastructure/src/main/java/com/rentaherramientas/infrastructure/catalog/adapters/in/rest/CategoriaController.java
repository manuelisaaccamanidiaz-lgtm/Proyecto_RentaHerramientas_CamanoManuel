package com.rentaherramientas.infrastructure.catalog.adapters.in.rest;

import com.rentaherramientas.application.catalog.dto.CategoriaResponse;
import com.rentaherramientas.application.catalog.usecase.ActualizarCategoriaUseCase;
import com.rentaherramientas.application.catalog.usecase.ConsultarCategoriaUseCase;
import com.rentaherramientas.application.catalog.usecase.CrearCategoriaUseCase;
import com.rentaherramientas.application.catalog.usecase.EliminarCategoriaUseCase;
import io.swagger.v3.oas.annotations.Operation;
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

    public CategoriaController(CrearCategoriaUseCase crearCategoria, ActualizarCategoriaUseCase actualizarCategoria,
                               EliminarCategoriaUseCase eliminarCategoria,
                               ConsultarCategoriaUseCase consultarCategoria) {
        this.crearCategoria = crearCategoria;
        this.actualizarCategoria = actualizarCategoria;
        this.eliminarCategoria = eliminarCategoria;
        this.consultarCategoria = consultarCategoria;
    }

    @GetMapping
    @SecurityRequirements
    @Operation(summary = "Lista todas las categorías (público)")
    public List<CategoriaResponse> listar() {
        return consultarCategoria.listar();
    }

    @GetMapping("/{id}")
    @SecurityRequirements
    @Operation(summary = "Detalle de una categoría (público)")
    public CategoriaResponse buscarPorId(@PathVariable Long id) {
        return consultarCategoria.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea una categoría (solo ADMIN)")
    public CategoriaResponse crear(@Valid @RequestBody CategoriaRequest request) {
        return crearCategoria.crear(request.toCommand());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza una categoría (solo ADMIN)")
    public CategoriaResponse actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
        return actualizarCategoria.actualizar(id, request.toCommand());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina una categoría sin herramientas (solo ADMIN); si está en uso responde 409")
    public void eliminar(@PathVariable Long id) {
        eliminarCategoria.eliminar(id);
    }
}
