package com.rentaherramientas.application.catalog.usecase;

import com.rentaherramientas.application.catalog.command.GuardarCategoriaCommand;
import com.rentaherramientas.application.catalog.dto.CategoriaResponse;
import com.rentaherramientas.application.catalog.exception.CategoriaDuplicadaException;
import com.rentaherramientas.application.catalog.exception.CategoriaNoEncontradaException;
import com.rentaherramientas.domain.catalog.model.aggregate.Categoria;
import com.rentaherramientas.domain.catalog.port.repository.CategoriaRepository;

/**
 * Actualiza una categoría. Conservar su propio nombre no cuenta como duplicado;
 * usar el nombre de OTRA categoría sí.
 */
public class ActualizarCategoriaService implements ActualizarCategoriaUseCase {

    private final CategoriaRepository categoriaRepository;

    /** Recibe sus dependencias por constructor; el bean lo crea CatalogBeansConfig. */
    public ActualizarCategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    /**
     * Renombra y cambia la descripción; falla si otra categoría ya usa el nuevo nombre.
     *
     * TÉCNICA AVANZADA (Java): encadenar `Optional`
     *   Qué hace: `buscarPorNombre(...).filter(...).ifPresent(...)` solo lanza la excepción si existe una
     *     categoría con ese nombre y además no es la misma que se edita, sin ningún `if`.
     */
    @Override
    public CategoriaResponse actualizar(Long id, GuardarCategoriaCommand command) {
        Categoria categoria = categoriaRepository.buscarPorId(id)
                .orElseThrow(() -> new CategoriaNoEncontradaException(id));
        categoria.renombrar(command.nombre());
        categoria.cambiarDescripcion(command.descripcion());
        categoriaRepository.buscarPorNombre(categoria.getNombre())
                .filter(otra -> !otra.getId().equals(id))
                .ifPresent(otra -> {
                    throw new CategoriaDuplicadaException(categoria.getNombre());
                });
        return CategoriaResponse.desde(categoriaRepository.guardar(categoria));
    }
}
