package com.rentaherramientas.infrastructure.billing.adapters.out.persistence;

import com.rentaherramientas.domain.billing.model.aggregate.Factura;
import com.rentaherramientas.domain.billing.model.valueobject.NumeroFactura;
import com.rentaherramientas.domain.billing.port.repository.FacturaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(factura)));
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
}
