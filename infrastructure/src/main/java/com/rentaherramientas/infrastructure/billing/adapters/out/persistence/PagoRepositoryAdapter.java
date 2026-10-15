package com.rentaherramientas.infrastructure.billing.adapters.out.persistence;

import com.rentaherramientas.domain.billing.model.aggregate.Pago;
import com.rentaherramientas.domain.billing.model.valueobject.ReferenciaPago;
import com.rentaherramientas.domain.billing.port.repository.PagoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida que implementa el puerto PagoRepository con JPA.
 */
@Repository
public class PagoRepositoryAdapter implements PagoRepository {

    private final PagoJpaRepository jpaRepository;
    private final PagoPersistenceMapper mapper;

    public PagoRepositoryAdapter(PagoJpaRepository jpaRepository, PagoPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Pago guardar(Pago pago) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(pago)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pago> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pago> buscarPorReferencia(ReferenciaPago referencia) {
        return jpaRepository.findByReferencia(referencia.valor()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pago> buscarPorReservaId(Long reservaId) {
        return jpaRepository.findByReservaIdOrderByFechaAsc(reservaId).stream().map(mapper::toDomain).toList();
    }
}
