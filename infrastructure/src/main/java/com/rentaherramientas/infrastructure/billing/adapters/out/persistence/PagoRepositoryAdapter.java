package com.rentaherramientas.infrastructure.billing.adapters.out.persistence;

import com.rentaherramientas.domain.billing.exception.PagoDuplicadoException;
import com.rentaherramientas.domain.billing.model.aggregate.Pago;
import com.rentaherramientas.domain.billing.model.valueobject.EstadoPago;
import com.rentaherramientas.domain.billing.model.valueobject.ReferenciaPago;
import com.rentaherramientas.domain.billing.port.repository.CriteriosBusquedaPago;
import com.rentaherramientas.domain.billing.port.repository.PagoRepository;
import com.rentaherramientas.domain.shared.model.Pagina;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Adaptador de salida que implementa el puerto PagoRepository con JPA.
 */
@Repository
public class PagoRepositoryAdapter implements PagoRepository {

    /** Índice único parcial de V6: un solo pago PENDIENTE o APROBADO por reserva. */
    static final String INDICE_PAGO_VIGENTE = "uq_pagos_reserva_vigente";
    private static final List<EstadoPago> ESTADOS_VIGENTES = List.of(EstadoPago.PENDIENTE, EstadoPago.APROBADO);

    private final PagoJpaRepository jpaRepository;
    private final PagoPersistenceMapper mapper;

    public PagoRepositoryAdapter(PagoJpaRepository jpaRepository, PagoPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    /**
     * saveAndFlush fuerza el INSERT dentro del método: si dos pagos simultáneos
     * pasan la comprobación del caso de uso, el índice único parcial rechaza al
     * segundo y aquí se traduce a la excepción de dominio (409).
     */
    @Override
    @Transactional
    public Pago guardar(Pago pago) {
        try {
            return mapper.toDomain(jpaRepository.saveAndFlush(mapper.toEntity(pago)));
        } catch (DataIntegrityViolationException e) {
            if (violaIndice(e, INDICE_PAGO_VIGENTE)) {
                throw new PagoDuplicadoException(pago.getReservaId());
            }
            throw e;
        }
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

    @Override
    @Transactional(readOnly = true)
    public boolean existePagoVigente(Long reservaId) {
        return jpaRepository.existsByReservaIdAndEstadoIn(reservaId, ESTADOS_VIGENTES);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePagoAprobado(Long reservaId) {
        return jpaRepository.existsByReservaIdAndEstadoIn(reservaId, List.of(EstadoPago.APROBADO));
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> reservasConPagoAprobado(Collection<Long> reservaIds) {
        if (reservaIds.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(jpaRepository.reservasConEstado(reservaIds, EstadoPago.APROBADO));
    }

    @Override
    @Transactional(readOnly = true)
    public Pagina<Pago> buscar(CriteriosBusquedaPago criterios) {
        int numero = criterios.pagina().numero();
        int tamano = criterios.pagina().tamano();
        Page<PagoEntity> pagina = jpaRepository.findAll(PagoSpecifications.segun(criterios),
                PageRequest.of(numero, tamano, Sort.by(Sort.Order.desc("fecha"), Sort.Order.desc("id"))));
        return new Pagina<>(pagina.getContent().stream().map(mapper::toDomain).toList(), numero, tamano,
                pagina.getTotalElements());
    }

    private static boolean violaIndice(Throwable error, String indice) {
        for (Throwable causa = error; causa != null; causa = causa.getCause()) {
            if (causa instanceof ConstraintViolationException violacion
                    && indice.equalsIgnoreCase(violacion.getConstraintName())) {
                return true;
            }
        }
        return false;
    }
}
