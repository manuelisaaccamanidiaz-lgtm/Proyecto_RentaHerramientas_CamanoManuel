package com.rentaherramientas.infrastructure.billing.adapters.out.persistence;

import com.rentaherramientas.domain.billing.model.aggregate.Factura;
import com.rentaherramientas.domain.billing.model.valueobject.NumeroFactura;
import com.rentaherramientas.domain.billing.port.repository.FacturaRepository;
import com.rentaherramientas.domain.shared.model.Pagina;
import com.rentaherramientas.domain.shared.model.SolicitudPagina;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida que implementa el puerto FacturaRepository con JPA.
 * Las lecturas son transaccionales porque las líneas se cargan de forma perezosa.
 */
@Repository
public class FacturaRepositoryAdapter implements FacturaRepository {

    private final FacturaJpaRepository jpaRepository;
    private final FacturaPersistenceMapper mapper;

    public FacturaRepositoryAdapter(FacturaJpaRepository jpaRepository, FacturaPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Factura guardar(Factura factura) {
        return mapper.toDomain(jpaRepository.saveAndFlush(mapper.toEntity(factura)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Factura> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Factura> buscarPorNumero(NumeroFactura numero) {
        return jpaRepository.findByNumero(numero.valor()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorPagoId(Long pagoId) {
        return jpaRepository.existsByPagoId(pagoId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Factura> buscarPorPagoId(Long pagoId) {
        return jpaRepository.findByPagoId(pagoId).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Factura> buscarPorPagoIds(Collection<Long> pagoIds) {
        if (pagoIds.isEmpty()) {
            return List.of();
        }
        return jpaRepository.buscarPorPagosConLineas(pagoIds).stream().map(mapper::toDomain).toList();
    }

    /**
     * Evita el N+1: página de facturas (más su COUNT) sin líneas y, después, una
     * sola consulta que inicializa las líneas de todas las facturas de la página.
     */
    @Override
    @Transactional(readOnly = true)
    public Pagina<Factura> buscar(Collection<Long> reservaIds, SolicitudPagina solicitud) {
        PageRequest pageRequest = PageRequest.of(solicitud.numero(), solicitud.tamano(),
                Sort.by(Sort.Order.desc("fechaEmision"), Sort.Order.desc("id")));
        Page<FacturaEntity> pagina;
        if (reservaIds == null) {
            pagina = jpaRepository.findAll(pageRequest);
        } else if (reservaIds.isEmpty()) {
            return new Pagina<>(List.of(), solicitud.numero(), solicitud.tamano(), 0);
        } else {
            pagina = jpaRepository.buscarPorReservas(reservaIds, pageRequest);
        }
        List<Long> ids = pagina.getContent().stream().map(FacturaEntity::getId).toList();
        if (!ids.isEmpty()) {
            jpaRepository.cargarConLineas(ids);
        }
        return new Pagina<>(pagina.getContent().stream().map(mapper::toDomain).toList(), solicitud.numero(),
                solicitud.tamano(), pagina.getTotalElements());
    }
}
