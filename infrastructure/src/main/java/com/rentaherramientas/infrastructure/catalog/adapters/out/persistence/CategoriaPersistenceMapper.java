package com.rentaherramientas.infrastructure.catalog.adapters.out.persistence;

import com.rentaherramientas.domain.catalog.model.aggregate.Categoria;
import org.springframework.stereotype.Component;

/**
 * Traduce entre el agregado Categoria y CategoriaEntity.
 */
@Component
public class CategoriaPersistenceMapper {

    /** Convierte la entidad en el agregado Categoria. */
    public Categoria toDomain(CategoriaEntity entity) {
        return Categoria.reconstruir(entity.getId(), entity.getNombre(), entity.getDescripcion());
    }

    /** Convierte el agregado en entidad JPA. */
    public CategoriaEntity toEntity(Categoria categoria) {
        return new CategoriaEntity(categoria.getId(), categoria.getNombre(), categoria.getDescripcion());
    }
}
