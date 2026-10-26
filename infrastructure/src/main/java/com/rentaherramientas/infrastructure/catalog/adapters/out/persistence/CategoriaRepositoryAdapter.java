package com.rentaherramientas.infrastructure.catalog.adapters.out.persistence;

import com.rentaherramientas.domain.catalog.model.aggregate.Categoria;
import com.rentaherramientas.domain.catalog.port.repository.CategoriaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Adaptador de salida que implementa el puerto CategoriaRepository con JPA.
 */
@Repository
public class CategoriaRepositoryAdapter implements CategoriaRepository {

    private final CategoriaJpaRepository jpaRepository;
    private final CategoriaPersistenceMapper mapper;

    /** Recibe el repositorio de Spring Data y el mapper por constructor. */
    public CategoriaRepositoryAdapter(CategoriaJpaRepository jpaRepository, CategoriaPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    /** Guarda la categoría (INSERT o UPDATE) y devuelve el agregado con id. */
    @Override
    @Transactional
    public Categoria guardar(Categoria categoria) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(categoria)));
    }

    /** Busca por id y convierte la entidad en agregado. */
    @Override
    @Transactional(readOnly = true)
    public Optional<Categoria> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    /** Todas las categorías ordenadas por nombre. */
    @Override
    @Transactional(readOnly = true)
    public List<Categoria> buscarTodas() {
        return jpaRepository.findAll(Sort.by("nombre")).stream().map(mapper::toDomain).toList();
    }

    /** Indica si el nombre (recortado) ya está en uso. */
    @Override
    @Transactional(readOnly = true)
    public boolean existePorNombre(String nombre) {
        return jpaRepository.existsByNombre(nombre == null ? null : nombre.strip());
    }

    /** Busca por nombre (recortado). */
    @Override
    @Transactional(readOnly = true)
    public Optional<Categoria> buscarPorNombre(String nombre) {
        return jpaRepository.findByNombre(nombre == null ? null : nombre.strip()).map(mapper::toDomain);
    }

    /** Indica si existen todas las categorías indicadas; quita duplicados antes de contar. */
    @Override
    @Transactional(readOnly = true)
    public boolean existenTodas(Collection<Long> ids) {
        Set<Long> distintos = new HashSet<>(ids);
        return distintos.isEmpty() || jpaRepository.countByIdIn(distintos) == distintos.size();
    }

    /** Indica si alguna herramienta usa la categoría. */
    @Override
    @Transactional(readOnly = true)
    public boolean estaEnUso(Long categoriaId) {
        return jpaRepository.estaEnUso(categoriaId);
    }

    /**
     * El flush fuerza el DELETE dentro del método: si la FK lo rechaza, la
     * excepción sale ya traducida a DataIntegrityViolationException.
     */
    @Override
    @Transactional
    public void eliminar(Long id) {
        jpaRepository.deleteById(id);
        jpaRepository.flush();
    }
}
