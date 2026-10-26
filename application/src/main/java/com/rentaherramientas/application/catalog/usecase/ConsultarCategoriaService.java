package com.rentaherramientas.application.catalog.usecase;

import com.rentaherramientas.application.catalog.dto.CategoriaResponse;
import com.rentaherramientas.application.catalog.exception.CategoriaNoEncontradaException;
import com.rentaherramientas.domain.catalog.port.repository.CategoriaRepository;

import java.util.List;

/**
 * Consultas de solo lectura sobre categorías. El listado no se pagina: el número
 * de categorías es pequeño y el frontend lo usa entero para los filtros.
 */
public class ConsultarCategoriaService implements ConsultarCategoriaUseCase {

    private final CategoriaRepository categoriaRepository;

    /** Recibe sus dependencias por constructor; el bean lo crea CatalogBeansConfig. */
    public ConsultarCategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    /** Devuelve la categoría o lanza CategoriaNoEncontradaException (404). */
    @Override
    public CategoriaResponse buscarPorId(Long id) {
        return categoriaRepository.buscarPorId(id)
                .map(CategoriaResponse::desde)
                .orElseThrow(() -> new CategoriaNoEncontradaException(id));
    }

    /** Devuelve todas las categorías. */
    @Override
    public List<CategoriaResponse> listar() {
        return categoriaRepository.buscarTodas().stream()
                .map(CategoriaResponse::desde)
                .toList();
    }
}
