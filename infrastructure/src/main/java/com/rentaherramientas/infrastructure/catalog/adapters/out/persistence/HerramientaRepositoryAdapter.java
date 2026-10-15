package com.rentaherramientas.infrastructure.catalog.adapters.out.persistence;

import com.rentaherramientas.domain.catalog.model.aggregate.Herramienta;
import com.rentaherramientas.domain.catalog.model.valueobject.EstadoHerramienta;
import com.rentaherramientas.domain.catalog.port.repository.CriteriosBusquedaHerramienta;
import com.rentaherramientas.domain.catalog.port.repository.HerramientaRepository;
import com.rentaherramientas.domain.shared.model.Pagina;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    /**
     * Evita el N+1 en tres pasos: (1) página de herramientas filtrada (más su
     * COUNT), sin colecciones; (2) y (3) una consulta por colección para todas
     * las herramientas de la página. Las tres devuelven las mismas instancias
     * gestionadas, así que al mapear ya no se dispara ninguna carga perezosa.
     */
    @Override
    @Transactional(readOnly = true)
    public Pagina<Herramienta> buscar(CriteriosBusquedaHerramienta criterios) {
        int numero = criterios.pagina().numero();
        int tamano = criterios.pagina().tamano();
        Page<HerramientaEntity> pagina = jpaRepository.findAll(HerramientaSpecifications.segun(criterios),
                PageRequest.of(numero, tamano, Sort.by("id")));
        List<Long> ids = pagina.getContent().stream().map(HerramientaEntity::getId).toList();
        if (!ids.isEmpty()) {
            jpaRepository.cargarConImagenes(ids);
            jpaRepository.cargarConCategorias(ids);
        }
        List<Herramienta> contenido = pagina.getContent().stream().map(mapper::toDomain).toList();
        return new Pagina<>(contenido, numero, tamano, pagina.getTotalElements());
    }
}
