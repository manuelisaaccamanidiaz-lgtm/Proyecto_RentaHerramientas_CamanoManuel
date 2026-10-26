package com.rentaherramientas.application.catalog.usecase;

import com.rentaherramientas.application.catalog.command.GuardarCategoriaCommand;
import com.rentaherramientas.application.catalog.dto.CategoriaResponse;
import com.rentaherramientas.application.catalog.exception.CategoriaDuplicadaException;
import com.rentaherramientas.domain.catalog.model.aggregate.Categoria;
import com.rentaherramientas.domain.catalog.port.repository.CategoriaRepository;

/**
 * Crea una categoría. El agregado se construye primero para validar y normalizar
 * el nombre; luego se comprueba que no esté repetido. La restricción UNIQUE de la
 * BD cubre la carrera entre dos altas simultáneas.
 */
public class CrearCategoriaService implements CrearCategoriaUseCase {

    private final CategoriaRepository categoriaRepository;

    /** Recibe sus dependencias por constructor; el bean lo crea CatalogBeansConfig. */
    public CrearCategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    /** Crea una categoría nueva si su nombre no está en uso. */
    @Override
    public CategoriaResponse crear(GuardarCategoriaCommand command) {
        Categoria categoria = Categoria.crear(command.nombre(), command.descripcion());
        if (categoriaRepository.existePorNombre(categoria.getNombre())) {
            throw new CategoriaDuplicadaException(categoria.getNombre());
        }
        return CategoriaResponse.desde(categoriaRepository.guardar(categoria));
    }
}
