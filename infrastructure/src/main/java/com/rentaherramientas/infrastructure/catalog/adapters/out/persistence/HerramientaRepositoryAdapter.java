package com.rentaherramientas.infrastructure.catalog.adapters.out.persistence;

import com.rentaherramientas.domain.catalog.model.aggregate.Herramienta;
import com.rentaherramientas.domain.catalog.model.valueobject.EstadoHerramienta;
import com.rentaherramientas.domain.catalog.port.repository.HerramientaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida que implementa el puerto HerramientaRepository con JPA.
 * Las lecturas son transaccionales porque imágenes y categorías se cargan de
 * forma perezosa y el mapper las recorre para reconstruir el agregado completo.
 */
@Repository
public class HerramientaRepositoryAdapter implements HerramientaRepository {

    private final HerramientaJpaRepository jpaRepository;
    private final HerramientaPersistenceMapper mapper;

    public HerramientaRepositoryAdapter(HerramientaJpaRepository jpaRepository,
                                        HerramientaPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Herramienta guardar(Herramienta herramienta) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(herramienta)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Herramienta> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Herramienta> buscarPorProveedorId(Long proveedorId) {
        return jpaRepository.findByProveedorId(proveedorId).stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Herramienta> buscarPorEstado(EstadoHerramienta estado) {
        return jpaRepository.findByEstado(estado).stream().map(mapper::toDomain).toList();
    }
}
