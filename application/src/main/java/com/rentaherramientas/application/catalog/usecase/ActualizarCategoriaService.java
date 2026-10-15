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

    public ActualizarCategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

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
