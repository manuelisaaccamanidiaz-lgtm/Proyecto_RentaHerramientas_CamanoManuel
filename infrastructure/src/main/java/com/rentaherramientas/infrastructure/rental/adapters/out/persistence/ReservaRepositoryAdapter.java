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

    /**
     * Qué estados ocupan la herramienta lo decide el dominio, no la consulta.
     *
     * TÉCNICA AVANZADA (Java): `Arrays.stream(Enum.values())`
     *   Qué hace: recorre todas las constantes del enum y se queda con las que cumplen
     *     `bloqueaDisponibilidad`. Se calcula una sola vez al cargar la clase porque es `static final`.
     */
    private static final List<EstadoReserva> ESTADOS_QUE_BLOQUEAN = Arrays.stream(EstadoReserva.values())
            .filter(EstadoReserva::bloqueaDisponibilidad)
            .toList();
    /**
     * exclusion_violation de PostgreSQL: la lanza ex_reservas_sin_solapamiento (V5).
     *
     * TÉCNICA AVANZADA (PostgreSQL): SQLSTATE
     *   Qué hace: código estándar de 5 caracteres que identifica el tipo de error, sin depender del idioma
     *     del mensaje. La clase `23` agrupa las violaciones de integridad (`23505` UNIQUE, `23503` FK,
     *     `23514` CHECK) y `23P01` es la violación de una restricción `EXCLUDE`, propia de PostgreSQL (la
     *     "P"). Se obtiene con `SQLException.getSQLState()`.
     */
    private static final String SQLSTATE_EXCLUSION_VIOLATION = "23P01";

    private final ReservaJpaRepository jpaRepository;
    private final ReservaPersistenceMapper mapper;

    /** Recibe el repositorio de Spring Data y el mapper por constructor. */
    public ReservaRepositoryAdapter(ReservaJpaRepository jpaRepository, ReservaPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    /**
     * saveAndFlush fuerza el INSERT/UPDATE dentro del método, así la violación de
     * la restricción de exclusión llega aquí y se traduce a la excepción de
     * dominio (409) en vez de salir como un error genérico al hacer commit.
     *
     * TÉCNICA AVANZADA (Spring Data): `saveAndFlush`
     *   Qué hace: `save` solo deja el cambio pendiente en memoria y el SQL se ejecuta al final de la
     *     transacción; `flush` lo envía a la base de datos en ese momento, así el error aparece dentro de
     *     este try/catch.
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

    /** Busca por id y convierte la entidad en agregado. */
    @Override
    @Transactional(readOnly = true)
    public Optional<Reserva> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    /** Reservas de un cliente, de la más reciente a la más antigua. */
    @Override
    @Transactional(readOnly = true)
    public List<Reserva> buscarPorClienteId(Long clienteId) {
        return jpaRepository.findByClienteIdOrderByCreadoEnDesc(clienteId).stream()
                .map(mapper::toDomain).toList();
    }

    /** Reservas de una herramienta, ordenadas por fecha de inicio. */
    @Override
    @Transactional(readOnly = true)
    public List<Reserva> buscarPorHerramientaId(Long herramientaId) {
        return jpaRepository.findByHerramientaIdOrderByFechaInicioAsc(herramientaId).stream()
                .map(mapper::toDomain).toList();
    }

    /** Ids de las reservas de un cliente. */
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

    /** Indica si alguna reserva que bloquea la herramienta se cruza con el rango. */
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

    /** Rangos de las reservas que bloquean la herramienta dentro del periodo. */
    @Override
    @Transactional(readOnly = true)
    public List<RangoFechas> rangosOcupados(Long herramientaId, RangoFechas periodo) {
        return jpaRepository.rangosOcupados(herramientaId, periodo.inicio(), periodo.fin(), ESTADOS_QUE_BLOQUEAN);
    }

    /** Reservas PENDIENTES o ACEPTADAS cuya fecha ya pasó. */
    @Override
    @Transactional(readOnly = true)
    public List<Reserva> buscarVencibles(LocalDate hoy) {
        return jpaRepository.buscarVencibles(hoy, EstadoReserva.PENDIENTE, EstadoReserva.ACEPTADA).stream()
                .map(mapper::toDomain).toList();
    }

    /**
     * Indica si la causa original del error es la violación de la restricción EXCLUDE (23P01).
     *
     * TÉCNICA AVANZADA (Java 16+): pattern matching de `instanceof`
     *   Qué hace: `causa instanceof SQLException sql` comprueba el tipo y, si coincide, declara la variable
     *     `sql` ya convertida, sin cast manual. El bucle `for` recorre la cadena de causas (`getCause()`)
     *     porque Spring envuelve la `SQLException` del driver.
     */
    private static boolean esViolacionDeExclusion(Throwable error) {
        for (Throwable causa = error; causa != null; causa = causa.getCause()) {
            if (causa instanceof SQLException sql && SQLSTATE_EXCLUSION_VIOLATION.equals(sql.getSQLState())) {
                return true;
            }
        }
        return false;
    }
}
