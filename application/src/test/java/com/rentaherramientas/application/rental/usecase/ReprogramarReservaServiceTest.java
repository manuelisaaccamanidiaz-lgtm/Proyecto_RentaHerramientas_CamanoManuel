package com.rentaherramientas.application.rental.usecase;

import com.rentaherramientas.application.rental.command.ReprogramarReservaCommand;
import com.rentaherramientas.application.rental.dto.ReservaResponse;
import com.rentaherramientas.application.rental.port.CatalogoHerramientas;
import com.rentaherramientas.application.rental.port.HerramientaReservable;
import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.application.shared.exception.AccesoDenegadoException;
import com.rentaherramientas.application.shared.port.PublicadorEventos;
import com.rentaherramientas.application.shared.port.UnidadDeTrabajo;
import com.rentaherramientas.domain.identity.model.valueobject.Rol;
import com.rentaherramientas.domain.rental.event.ReservaReprogramada;
import com.rentaherramientas.domain.rental.exception.ReservaSolapadaException;
import com.rentaherramientas.domain.rental.model.aggregate.Reserva;
import com.rentaherramientas.domain.rental.model.valueobject.EstadoReserva;
import com.rentaherramientas.domain.rental.model.valueobject.RangoFechas;
import com.rentaherramientas.domain.rental.port.repository.CriteriosBusquedaReserva;
import com.rentaherramientas.domain.rental.port.repository.ReservaRepository;
import com.rentaherramientas.domain.shared.event.DomainEvent;
import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;
import com.rentaherramientas.domain.shared.model.Pagina;
import com.rentaherramientas.domain.shared.model.valueobject.Dinero;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del caso de uso con dobles escritos a mano (sin Mockito ni Spring): un
 * repositorio de reservas en memoria que resuelve los cruces con las reglas del
 * dominio, un catálogo de una herramienta, un reloj fijo, una unidad de trabajo
 * que solo ejecuta la lambda y un publicador que acumula los eventos.
 */
class ReprogramarReservaServiceTest {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    /** "Hoy" fijo: 15 de octubre de 2026, mediodía en Bogotá. */
    private static final Clock RELOJ = Clock.fixed(
            LocalDateTime.of(2026, 10, 15, 12, 0).atZone(BOGOTA).toInstant(), BOGOTA);
    private static final LocalDate HOY = LocalDate.of(2026, 10, 15);

    private static final Long HERRAMIENTA_ID = 10L;
    private static final Long PROVEEDOR_ID = 2L;
    private static final Solicitante CLIENTE = new Solicitante(5L, Rol.CLIENTE);
    private static final Solicitante OTRO_CLIENTE = new Solicitante(6L, Rol.CLIENTE);
    private static final Dinero TARIFA_INICIAL = Dinero.de(new BigDecimal("20000"), "COP");
    /** La tarifa vigente al reprogramar: el precio se recalcula con esta, no con la original. */
    private static final Dinero TARIFA_ACTUAL = Dinero.de(new BigDecimal("25000"), "COP");

    private ReservaRepositoryEnMemoria reservas;
    private PublicadorEnMemoria publicador;
    private ReprogramarReservaService service;
    private Long reservaId;

    @BeforeEach
    void preparar() {
        reservas = new ReservaRepositoryEnMemoria();
        CatalogoDeUnaHerramienta catalogo = new CatalogoDeUnaHerramienta(
                new HerramientaReservable(HERRAMIENTA_ID, PROVEEDOR_ID, TARIFA_ACTUAL, true));
        publicador = new PublicadorEnMemoria();
        service = new ReprogramarReservaService(reservas, catalogo, new UnidadDeTrabajoDirecta(), publicador, RELOJ);
        // Reserva PENDIENTE del cliente del 5 al 7 (3 días a la tarifa inicial).
        reservaId = reservas.guardar(Reserva.registrar(HERRAMIENTA_ID, CLIENTE.id(),
                new RangoFechas(HOY.plusDays(5), HOY.plusDays(7)), TARIFA_INICIAL)).getId();
    }

    private ReprogramarReservaCommand comando(Solicitante solicitante, LocalDate inicio, LocalDate fin) {
        return new ReprogramarReservaCommand(solicitante, reservaId, inicio, fin);
    }

    @Test
    @DisplayName("Caso feliz: cambia las fechas, recalcula con la tarifa actual y publica ReservaReprogramada")
    void casoFeliz() {
        ReservaResponse respuesta = service.reprogramar(comando(CLIENTE, HOY.plusDays(10), HOY.plusDays(11)));

        assertEquals(HOY.plusDays(10), respuesta.fechaInicio());
        assertEquals(2, respuesta.dias());
        assertEquals(new BigDecimal("50000.00"), respuesta.precioTotal());
        assertEquals(EstadoReserva.PENDIENTE, respuesta.estado());
        ReservaReprogramada evento = assertInstanceOf(ReservaReprogramada.class, publicador.publicados.getFirst());
        assertEquals(reservaId, evento.reservaId());
        assertEquals(new RangoFechas(HOY.plusDays(5), HOY.plusDays(7)), evento.rangoAnterior());
    }

    @Test
    @DisplayName("No puede reprogramar la reserva de otro cliente → AccesoDenegadoException (403)")
    void otroCliente() {
        assertThrows(AccesoDenegadoException.class,
                () -> service.reprogramar(comando(OTRO_CLIENTE, HOY.plusDays(10), HOY.plusDays(11))));
        assertEquals(HOY.plusDays(5), reservas.buscarPorId(reservaId).orElseThrow().getRango().inicio());
        assertTrue(publicador.publicados.isEmpty());
    }

    @Test
    @DisplayName("No se cruza consigo misma: moverla un día (solapando sus fechas antiguas) se permite")
    void noSeCruzaConsigoMisma() {
        ReservaResponse respuesta = service.reprogramar(comando(CLIENTE, HOY.plusDays(6), HOY.plusDays(8)));

        assertEquals(HOY.plusDays(6), respuesta.fechaInicio());
        assertEquals(new BigDecimal("75000.00"), respuesta.precioTotal());
    }

    @Test
    @DisplayName("Sí detecta el cruce con OTRA reserva activa → ReservaSolapadaException (409) y no cambia nada")
    void cruceConOtra() {
        reservas.guardar(Reserva.registrar(HERRAMIENTA_ID, 99L, new RangoFechas(HOY.plusDays(12), HOY.plusDays(14)),
                TARIFA_INICIAL));

        assertThrows(ReservaSolapadaException.class,
                () -> service.reprogramar(comando(CLIENTE, HOY.plusDays(10), HOY.plusDays(12))));
        assertEquals(HOY.plusDays(5), reservas.buscarPorId(reservaId).orElseThrow().getRango().inicio());
        assertTrue(publicador.publicados.isEmpty());
    }

    @Test
    @DisplayName("Reutiliza las reglas de fechas de crear: en el pasado o más de 30 días → 400")
    void reglasDeFechas() {
        assertThrows(ValorInvalidoException.class,
                () -> service.reprogramar(comando(CLIENTE, HOY.minusDays(1), HOY.plusDays(1))));
        assertThrows(ValorInvalidoException.class,
                () -> service.reprogramar(comando(CLIENTE, HOY, HOY.plusDays(30))));
    }

    // ------------------------------------------------------- dobles de prueba

    /** Ejecuta la lambda tal cual. */
    private static final class UnidadDeTrabajoDirecta implements UnidadDeTrabajo {

        @Override
        public <T> T ejecutar(Supplier<T> operacion) {
            return operacion.get();
        }
    }

    /** Acumula los eventos publicados. */
    private static final class PublicadorEnMemoria implements PublicadorEventos {

        private final List<DomainEvent> publicados = new ArrayList<>();

        @Override
        public void publicar(List<DomainEvent> eventos) {
            publicados.addAll(eventos);
        }
    }

    /** Catálogo con una sola herramienta. */
    private record CatalogoDeUnaHerramienta(HerramientaReservable herramienta) implements CatalogoHerramientas {

        @Override
        public Optional<HerramientaReservable> buscar(Long herramientaId) {
            return herramienta.id().equals(herramientaId) ? Optional.of(herramienta) : Optional.empty();
        }

        @Override
        public List<Long> idsDelProveedor(Long proveedorId) {
            throw new UnsupportedOperationException("no lo usa ReprogramarReservaService");
        }

        @Override
        public void marcarDanada(Long herramientaId) {
            throw new UnsupportedOperationException("no lo usa ReprogramarReservaService");
        }
    }

    /**
     * Repositorio en memoria: asigna ids y resuelve cruces con las reglas del dominio. Como el adaptador
     * real, devuelve una copia reconstruida, así la reserva que modifica el caso de uso no es la guardada.
     */
    private static final class ReservaRepositoryEnMemoria implements ReservaRepository {

        private final List<Reserva> guardadas = new ArrayList<>();

        @Override
        public Reserva guardar(Reserva reserva) {
            Long id = reserva.getId() != null ? reserva.getId() : (long) guardadas.size() + 1;
            Reserva copia = Reserva.reconstruir(id, reserva.getHerramientaId(), reserva.getClienteId(),
                    reserva.getRango(), reserva.getEstado(), reserva.getPrecioTotal(), reserva.getFechaEntrega(),
                    reserva.getFechaDevolucion(), reserva.getCreadoEn(), reserva.getReportes());
            guardadas.removeIf(r -> r.getId().equals(id));
            guardadas.add(copia);
            return copia;
        }

        @Override
        public Optional<Reserva> buscarPorId(Long id) {
            // Copia: el caso de uso no debe modificar la "fila" guardada sin pasar por guardar.
            return guardadas.stream().filter(r -> r.getId().equals(id)).findFirst()
                    .map(r -> Reserva.reconstruir(r.getId(), r.getHerramientaId(), r.getClienteId(), r.getRango(),
                            r.getEstado(), r.getPrecioTotal(), r.getFechaEntrega(), r.getFechaDevolucion(),
                            r.getCreadoEn(), r.getReportes()));
        }

        @Override
        public boolean existeSolapamiento(Long herramientaId, RangoFechas rango) {
            return existeSolapamientoExcluyendo(herramientaId, rango, null);
        }

        @Override
        public boolean existeSolapamientoExcluyendo(Long herramientaId, RangoFechas rango, Long reservaId) {
            return guardadas.stream().anyMatch(r -> r.getHerramientaId().equals(herramientaId)
                    && !r.getId().equals(reservaId)
                    && r.getEstado().bloqueaDisponibilidad()
                    && r.seCruzaCon(rango));
        }

        @Override
        public long contarPorClienteYEstado(Long clienteId, EstadoReserva estado) {
            throw new UnsupportedOperationException("no lo usa ReprogramarReservaService");
        }

        @Override
        public List<Reserva> buscarPorClienteId(Long clienteId) {
            throw new UnsupportedOperationException("no lo usa ReprogramarReservaService");
        }

        @Override
        public List<Reserva> buscarPorHerramientaId(Long herramientaId) {
            throw new UnsupportedOperationException("no lo usa ReprogramarReservaService");
        }

        @Override
        public List<Long> idsPorCliente(Long clienteId) {
            throw new UnsupportedOperationException("no lo usa ReprogramarReservaService");
        }

        @Override
        public List<Long> idsPorHerramientas(Collection<Long> herramientaIds) {
            throw new UnsupportedOperationException("no lo usa ReprogramarReservaService");
        }

        @Override
        public Pagina<Reserva> buscar(CriteriosBusquedaReserva criterios) {
            throw new UnsupportedOperationException("no lo usa ReprogramarReservaService");
        }

        @Override
        public List<RangoFechas> rangosOcupados(Long herramientaId, RangoFechas periodo) {
            throw new UnsupportedOperationException("no lo usa ReprogramarReservaService");
        }

        @Override
        public List<Reserva> buscarVencibles(LocalDate hoy) {
            throw new UnsupportedOperationException("no lo usa ReprogramarReservaService");
        }
    }
}
