package com.rentaherramientas.application.catalog.usecase;

import com.rentaherramientas.application.catalog.exception.CategoriaEnUsoException;
import com.rentaherramientas.application.catalog.exception.CategoriaNoEncontradaException;
import com.rentaherramientas.domain.catalog.model.aggregate.Categoria;
import com.rentaherramientas.domain.catalog.port.repository.CategoriaRepository;

/**
 * Elimina una categoría que ninguna herramienta usa. La comprobación previa da un
 * 409 con un mensaje claro; la FK fk_herramienta_categorias_categoria (sin cascade)
 * sigue siendo la red de seguridad si otra petición asigna la categoría entre la
 * comprobación y el DELETE.
 */
public class EliminarCategoriaService implements EliminarCategoriaUseCase {

    private final CategoriaRepository categoriaRepository;

    /** Recibe sus dependencias por constructor; el bean lo crea CatalogBeansConfig. */
    public EliminarCategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    /** Elimina la categoría; lanza 404 si no existe y 409 si está en uso. */
    @Override
    public void eliminar(Long id) {
        Categoria categoria = categoriaRepository.buscarPorId(id)
                .orElseThrow(() -> new CategoriaNoEncontradaException(id));
        if (categoriaRepository.estaEnUso(id)) {
            throw new CategoriaEnUsoException(categoria.getNombre());
        }
        categoriaRepository.eliminar(id);
    }
}
