package com.rentaherramientas.domain.catalog.port.repository;

import com.rentaherramientas.domain.catalog.model.aggregate.Categoria;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida del agregado Categoria; lo implementa un adaptador JPA.
 */
public interface CategoriaRepository {

    Categoria guardar(Categoria categoria);

    Optional<Categoria> buscarPorId(Long id);

    List<Categoria> buscarTodas();

    boolean existePorNombre(String nombre);
}
