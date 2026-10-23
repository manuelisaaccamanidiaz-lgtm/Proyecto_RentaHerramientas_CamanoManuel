package com.rentaherramientas.infrastructure.catalog.adapters.out.persistence;

import com.rentaherramientas.domain.catalog.model.aggregate.Favorito;
import com.rentaherramientas.domain.catalog.port.repository.FavoritoRepository;
import com.rentaherramientas.domain.shared.model.Pagina;
import com.rentaherramientas.domain.shared.model.SolicitudPagina;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador de salida que implementa el puerto FavoritoRepository con JPA. La
 * idempotencia la da la base: ON CONFLICT DO NOTHING al insertar y un DELETE
 * que no falla si no encuentra la fila.
 */
@Repository
public class FavoritoRepositoryAdapter implements FavoritoRepository {

    private final FavoritoJpaRepository jpaRepository;

    /** Recibe el repositorio de Spring Data por constructor. */
    public FavoritoRepositoryAdapter(FavoritoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    /** Inserta el favorito si no existía. */
    @Override
    @Transactional
    public void agregar(Favorito favorito) {
        jpaRepository.insertarSiNoExiste(favorito.clienteId(), favorito.herramientaId(), favorito.creadoEn());
    }

    /** Borra el favorito si existía. */
    @Override
    @Transactional
    public void quitar(Long clienteId, Long herramientaId) {
        jpaRepository.eliminar(clienteId, herramientaId);
    }

    /** Página de ids de herramientas favoritas, la más reciente primero. */
    @Override
    @Transactional(readOnly = true)
    public Pagina<Long> herramientaIdsDe(Long clienteId, SolicitudPagina pagina) {
        Page<Long> ids = jpaRepository.herramientaIdsDe(clienteId, PageRequest.of(pagina.numero(), pagina.tamano()));
        return new Pagina<>(ids.getContent(), pagina.numero(), pagina.tamano(), ids.getTotalElements());
    }
}
