package com.rentaherramientas.infrastructure.rental.adapters.out.persistence;

import com.rentaherramientas.domain.rental.model.aggregate.Reserva;
import com.rentaherramientas.domain.rental.model.valueobject.EstadoReserva;
import com.rentaherramientas.domain.rental.model.valueobject.RangoFechas;
import com.rentaherramientas.domain.rental.port.repository.ReservaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida que implementa el puerto ReservaRepository con JPA.
 * Las lecturas son transaccionales porque los reportes se cargan de forma perezosa.
 */
@Repository
public class ReservaRepositoryAdapter implements ReservaRepository {

    /** Qué estados ocupan la herramienta lo decide el dominio, no la consulta. */
    private static final List<EstadoReserva> ESTADOS_QUE_BLOQUEAN = Arrays.stream(EstadoReserva.values())
            .filter(EstadoReserva::bloqueaDisponibilidad)
            .toList();

    private final ReservaJpaRepository jpaRepository;
    private final ReservaPersistenceMapper mapper;

    public ReservaRepositoryAdapter(ReservaJpaRepository jpaRepository, ReservaPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Reserva guardar(Reserva reserva) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(reserva)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Reserva> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reserva> buscarPorClienteId(Long clienteId) {
        return jpaRepository.findByClienteIdOrderByCreadoEnDesc(clienteId).stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reserva> buscarPorHerramientaId(Long herramientaId) {
        return jpaRepository.findByHerramientaIdOrderByFechaInicioAsc(herramientaId).stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeSolapamiento(Long herramientaId, RangoFechas rango) {
        return jpaRepository.existeSolapamiento(herramientaId, rango.inicio(), rango.fin(), ESTADOS_QUE_BLOQUEAN);
    }
}
