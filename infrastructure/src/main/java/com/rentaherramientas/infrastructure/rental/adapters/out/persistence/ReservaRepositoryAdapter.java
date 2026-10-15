package com.rentaherramientas.infrastructure.rental.adapters.out.persistence;

import com.rentaherramientas.domain.rental.exception.ReservaSolapadaException;
import com.rentaherramientas.domain.rental.model.aggregate.Reserva;
import com.rentaherramientas.domain.rental.model.valueobject.EstadoReserva;
import com.rentaherramientas.domain.rental.model.valueobject.RangoFechas;
import com.rentaherramientas.domain.rental.port.repository.CriteriosBusquedaReserva;
import com.rentaherramientas.domain.rental.port.repository.ReservaRepository;
import com.rentaherramientas.domain.shared.model.Pagina;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
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
    /** exclusion_violation de PostgreSQL: la lanza ex_reservas_sin_solapamiento (V5). */
    private static final String SQLSTATE_EXCLUSION_VIOLATION = "23P01";

    private final ReservaJpaRepository jpaRepository;
    private final ReservaPersistenceMapper mapper;

    public ReservaRepositoryAdapter(ReservaJpaRepository jpaRepository, ReservaPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    /**
     * saveAndFlush fuerza el INSERT/UPDATE dentro del método, así la violación de
     * la restricción de exclusión llega aquí y se traduce a la excepción de
     * dominio (409) en vez de salir como un error genérico al hacer commit.
     */
    @Override
    @Transactional
    public Reserva guardar(Reserva reserva) {
        try {
            return mapper.toDomain(jpaRepository.saveAndFlush(mapper.toEntity(reserva)));
        } catch (DataIntegrityViolationException e) {
            if (esViolacionDeExclusion(e)) {
                throw new ReservaSolapadaException(reserva.getHerramientaId(), reserva.getRango());
            }
            throw e;
        }
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
    public List<Long> idsPorCliente(Long clienteId) {
        return jpaRepository.idsPorCliente(clienteId);
    }

    /** Un IN vacío no es SQL válido: sin herramientas no hay reservas. */
    @Override
    @Transactional(readOnly = true)
    public List<Long> idsPorHerramientas(Collection<Long> herramientaIds) {
        return herramientaIds.isEmpty() ? List.of() : jpaRepository.idsPorHerramientas(herramientaIds);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeSolapamiento(Long herramientaId, RangoFechas rango) {
        return jpaRepository.existeSolapamiento(herramientaId, rango.inicio(), rango.fin(), ESTADOS_QUE_BLOQUEAN);
    }

    /**
     * Evita el N+1: (1) página de reservas filtrada (más su COUNT), sin reportes;
     * (2) una sola consulta que inicializa los reportes de todas las reservas de
     * la página, que son las mismas instancias gestionadas.
     */
    @Override
    @Transactional(readOnly = true)
    public Pagina<Reserva> buscar(CriteriosBusquedaReserva criterios) {
        int numero = criterios.pagina().numero();
        int tamano = criterios.pagina().tamano();
        Page<ReservaEntity> pagina = jpaRepository.findAll(ReservaSpecifications.segun(criterios),
                PageRequest.of(numero, tamano, Sort.by(Sort.Order.desc("creadoEn"), Sort.Order.desc("id"))));
        List<Long> ids = pagina.getContent().stream().map(ReservaEntity::getId).toList();
        if (!ids.isEmpty()) {
            jpaRepository.cargarConReportes(ids);
        }
        List<Reserva> contenido = pagina.getContent().stream().map(mapper::toDomain).toList();
        return new Pagina<>(contenido, numero, tamano, pagina.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RangoFechas> rangosOcupados(Long herramientaId, RangoFechas periodo) {
        return jpaRepository.rangosOcupados(herramientaId, periodo.inicio(), periodo.fin(), ESTADOS_QUE_BLOQUEAN);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reserva> buscarVencibles(LocalDate hoy) {
        return jpaRepository.buscarVencibles(hoy, EstadoReserva.PENDIENTE, EstadoReserva.ACEPTADA).stream()
                .map(mapper::toDomain).toList();
    }

    private static boolean esViolacionDeExclusion(Throwable error) {
        for (Throwable causa = error; causa != null; causa = causa.getCause()) {
            if (causa instanceof SQLException sql && SQLSTATE_EXCLUSION_VIOLATION.equals(sql.getSQLState())) {
                return true;
            }
        }
        return false;
    }
}
