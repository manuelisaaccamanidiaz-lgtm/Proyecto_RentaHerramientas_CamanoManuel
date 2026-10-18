package com.rentaherramientas.domain.catalog.port.repository;

import com.rentaherramientas.domain.catalog.model.aggregate.Categoria;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida del agregado Categoria; lo implementa un adaptador JPA.
 */
public interface CategoriaRepository {

    /** Inserta o actualiza la categoría y la devuelve con el id asignado. */
    Categoria guardar(Categoria categoria);

    /** Busca una categoría por id; vacío si no existe. */
    Optional<Categoria> buscarPorId(Long id);

    /** Todas las categorías ordenadas por nombre. */
    List<Categoria> buscarTodas();

    /** Indica si ya existe una categoría con ese nombre. */
    boolean existePorNombre(String nombre);

    /** Busca una categoría por nombre; sirve para detectar duplicados al renombrar. */
    Optional<Categoria> buscarPorNombre(String nombre);

    /** true si existen todas las categorías con esos ids (los duplicados se ignoran). */
    boolean existenTodas(Collection<Long> ids);

    /** Si la categoría está en uso por alguna herramienta, la FK de la BD impide borrarla. */
    void eliminar(Long id);
}
