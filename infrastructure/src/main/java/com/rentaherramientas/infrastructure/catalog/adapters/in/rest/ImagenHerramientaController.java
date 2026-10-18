package com.rentaherramientas.infrastructure.catalog.adapters.in.rest;

import com.rentaherramientas.application.catalog.command.AgregarImagenCommand;
import com.rentaherramientas.application.catalog.dto.HerramientaResponse;
import com.rentaherramientas.application.catalog.usecase.GestionarImagenesHerramientaUseCase;
import com.rentaherramientas.infrastructure.shared.adapters.in.rest.SolicitanteJwt;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Adaptador de entrada de la galería de imágenes. Solo traduce el multipart a
 * bytes + content type; formato, límite y propiedad los valida el caso de uso.
 */
@RestController
@RequestMapping("/api/herramientas/{id}/imagenes")
@Tag(name = "Imágenes de herramientas", description = "Galería de imágenes (máx. 5, JPEG/PNG/WebP hasta 5 MB)")
public class ImagenHerramientaController {

    private final GestionarImagenesHerramientaUseCase gestionarImagenes;

    /** Recibe el caso de uso por constructor (inyección de dependencias de Spring). */
    public ImagenHerramientaController(GestionarImagenesHerramientaUseCase gestionarImagenes) {
        this.gestionarImagenes = gestionarImagenes;
    }

    /**
     * POST /api/herramientas/{id}/imagenes : sube una imagen (multipart) y responde 201.
     *
     * TÉCNICA AVANZADA (Spring MVC): subida de archivos
     *   Qué hace: `consumes = MULTIPART_FORM_DATA_VALUE` acepta formularios `multipart/form-data` y
     *     `@RequestPart("archivo") MultipartFile` recibe el archivo del campo "archivo". El tamaño máximo lo
     *     fija `spring.servlet.multipart.max-file-size`.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Sube una imagen en el campo \"archivo\" (dueño o ADMIN)")
    public HerramientaResponse agregar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                       @RequestPart("archivo") MultipartFile archivo) throws IOException {
        return gestionarImagenes.agregar(new AgregarImagenCommand(id, SolicitanteJwt.desde(jwt),
                archivo.getBytes(), archivo.getContentType()));
    }

    /** DELETE /api/herramientas/{id}/imagenes/{orden} : quita la imagen de esa posición. */
    @DeleteMapping("/{orden}")
    @Operation(summary = "Quita la imagen de esa posición y borra su archivo (dueño o ADMIN)")
    public HerramientaResponse quitar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                      @PathVariable int orden) {
        return gestionarImagenes.quitar(id, orden, SolicitanteJwt.desde(jwt));
    }
}
