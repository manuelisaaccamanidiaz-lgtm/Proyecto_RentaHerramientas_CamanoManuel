package com.rentaherramientas.application.rental.usecase;

import com.rentaherramientas.application.rental.command.CrearReservaCommand;
import com.rentaherramientas.application.rental.dto.ReservaResponse;
import com.rentaherramientas.application.rental.exception.HerramientaNoDisponibleException;
import com.rentaherramientas.application.rental.exception.HerramientaNoEncontradaException;
import com.rentaherramientas.application.rental.exception.LimiteReservasPendientesException;
import com.rentaherramientas.application.rental.port.CatalogoHerramientas;
import com.rentaherramientas.application.rental.port.HerramientaReservable;
import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.application.shared.exception.AccesoDenegadoException;
import com.rentaherramientas.application.shared.port.PublicadorEventos;
import com.rentaherramientas.application.shared.port.UnidadDeTrabajo;
import com.rentaherramientas.domain.identity.model.valueobject.Rol;
import com.rentaherramientas.domain.rental.event.ReservaAceptada;
import com.rentaherramientas.domain.rental.event.ReservaSolicitada;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del caso de uso con dobles escritos a mano (sin Mockito ni Spring):
 * un repositorio en memoria, un catálogo en memoria, un reloj fijo, una unidad
 * de trabajo que solo ejecuta la lambda y un publicador que acumula los eventos.
 * Repositorio, unidad y publicador anotan en un mismo registro qué pasó y en qué
 * orden, para comprobar que los eventos se publican DESPUÉS de guardar.
 */
class CrearReservaServiceTest {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    /** "Hoy" fijo: 15 de octubre de 2026, mediodía en Bogotá. */
    private static final Clock RELOJ = Clock.fixed(
            LocalDateTime.of(2026, 10, 15, 12, 0).atZone(BOGOTA).toInstant(), BOGOTA);
    private static final LocalDate HOY = LocalDate.of(2026, 10, 15);

    private static final Long HERRAMIENTA_ID = 10L;
    private static final Long PROVEEDOR_ID = 2L;
    private static final Solicitante CLIENTE = new Solicitante(5L, Rol.CLIENTE);
    private static final Dinero TARIFA = Dinero.de(new BigDecimal("20000"), "COP");
    private static final int MAX_PENDIENTES = 3;

    private List<String> registro;
    private ReservaRepositoryEnMemoria reservas;
    private CatalogoEnMemoria catalogo;
    private UnidadDeTrabajoRegistrada unidadDeTrabajo;
    private PublicadorEnMemoria publicador;
    private CrearReservaService service;

    @BeforeEach
    void preparar() {
        registro = new ArrayList<>();
        reservas = new ReservaRepositoryEnMemoria(registro);
        catalogo = new CatalogoEnMemoria();
        catalogo.agregar(new HerramientaReservable(HERRAMIENTA_ID, PROVEEDOR_ID, TARIFA, true));
        unidadDeTrabajo = new UnidadDeTrabajoRegistrada(registro);
        publicador = new PublicadorEnMemoria(registro);
        service = new CrearReservaService(reservas, catalogo, unidadDeTrabajo, publicador, RELOJ,
                MAX_PENDIENTES);
    }

    private static CrearReservaCommand comando(LocalDate inicio, LocalDate fin) {
        return new CrearReservaCommand(CLIENTE, HERRAMIENTA_ID, inicio, fin);
    }

    @Test
    @DisplayName("Caso feliz: guarda una reserva PENDIENTE con precio = tarifa × días")
    void casoFeliz() {
        ReservaResponse respuesta = service.crear(comando(HOY, HOY.plusDays(2)));

        assertEquals(EstadoReserva.PENDIENTE, respuesta.estado());
        assertEquals(3, respuesta.dias());
        assertEquals(new BigDecimal("60000.00"), respuesta.precioTotal());
        assertEquals("COP", respuesta.moneda());
        assertEquals(CLIENTE.id(), respuesta.clienteId());
        assertEquals(1, reservas.guardadas.size(), "se guardó exactamente una reserva");
    }

    @Test
    @DisplayName("Con descuento: el catálogo ya entrega la tarifa rebajada y precio = tarifa con descuento × días")
    void tarifaConDescuento() {
        // 33333.33 con 15 % = 28333.33: es lo que entrega el adaptador anticorrupción; rental no ve el porcentaje.
        Dinero tarifaRebajada = Dinero.de(new BigDecimal("33333.33"), "COP").aplicarDescuento(15);
        catalogo.agregar(new HerramientaReservable(HERRAMIENTA_ID, PROVEEDOR_ID, tarifaRebajada, true));

        ReservaResponse respuesta = service.crear(comando(HOY, HOY.plusDays(2)));

        assertEquals(new BigDecimal("84999.99"), respuesta.precioTotal());
        assertEquals(tarifaRebajada.multiplicar(3).monto(), respuesta.precioTotal());
    }

    // -------------------------------------------------------------- eventos

    @Test
    @DisplayName("Publica exactamente un ReservaSolicitada, después de guardar y dentro de la unidad de trabajo")
    void publicaReservaSolicitada() {
        service.crear(comando(HOY, HOY.plusDays(2)));

        assertEquals(1, publicador.publicados.size(), "un solo evento");
        ReservaSolicitada evento = assertInstanceOf(ReservaSolicitada.class, publicador.publicados.getFirst());
        assertEquals(HERRAMIENTA_ID, evento.herramientaId());
        assertEquals(CLIENTE.id(), evento.clienteId());
        assertEquals(new RangoFechas(HOY, HOY.plusDays(2)), evento.rango());
        assertEquals(List.of("inicio unidad", "guardar", "publicar 1", "fin unidad"), registro);
    }

    @Test
    @DisplayName("Si una regla falla no se guarda ni se publica nada")
    void sinEventosSiFalla() {
        assertThrows(ValorInvalidoException.class, () -> service.crear(comando(HOY.minusDays(1), HOY)));

        assertTrue(publicador.publicados.isEmpty());
        assertTrue(registro.isEmpty());
    }

    @Test
    @DisplayName("Aceptar publica solo ReservaAceptada, con el id de la reserva, después de guardar")
    void aceptarPublicaReservaAceptada() {
        Long reservaId = service.crear(comando(HOY, HOY.plusDays(2))).id();
        publicador.publicados.clear();
        registro.clear();
        AceptarReservaService aceptar = new AceptarReservaService(reservas, catalogo, unidadDeTrabajo, publicador);

        aceptar.aceptar(reservaId, new Solicitante(PROVEEDOR_ID, Rol.PROVEEDOR));

        assertEquals(1, publicador.publicados.size(), "ReservaSolicitada no se vuelve a publicar");
        ReservaAceptada evento = assertInstanceOf(ReservaAceptada.class, publicador.publicados.getFirst());
        assertEquals(reservaId, evento.reservaId());
        assertEquals(HERRAMIENTA_ID, evento.herramientaId());
        assertEquals(CLIENTE.id(), evento.clienteId());
        assertEquals(List.of("inicio unidad", "guardar", "publicar 1", "fin unidad"), registro);
    }

    // ---------------------------------------------------------------- reglas

    @Test
    @DisplayName("Fecha de inicio anterior a hoy → ValorInvalidoException (400) y no guarda nada")
    void fechaPasada() {
        assertThrows(ValorInvalidoException.class, () -> service.crear(comando(HOY.minusDays(1), HOY.plusDays(1))));
        assertTrue(reservas.guardadas.isEmpty());
    }

    @Test
    @DisplayName("Herramienta no DISPONIBLE → HerramientaNoDisponibleException (409)")
    void herramientaNoDisponible() {
        catalogo.agregar(new HerramientaReservable(HERRAMIENTA_ID, PROVEEDOR_ID, TARIFA, false));

        assertThrows(HerramientaNoDisponibleException.class, () -> service.crear(comando(HOY, HOY.plusDays(1))));
        assertTrue(reservas.guardadas.isEmpty());
    }

    @Test
    @DisplayName("Fechas que se cruzan con otra reserva activa → ReservaSolapadaException (409)")
    void solapamiento() {
        reservas.guardar(Reserva.registrar(HERRAMIENTA_ID, 99L, new RangoFechas(HOY.plusDays(3), HOY.plusDays(6)),
                TARIFA));

        assertThrows(ReservaSolapadaException.class, () -> service.crear(comando(HOY.plusDays(5), HOY.plusDays(8))));
        assertEquals(1, reservas.guardadas.size(), "no se guardó la segunda reserva");
    }

    @Test
    @DisplayName("Una reserva RECHAZADA no bloquea las fechas")
    void reservaRechazadaNoBloquea() {
        Reserva previa = Reserva.registrar(HERRAMIENTA_ID, 99L, new RangoFechas(HOY, HOY.plusDays(2)), TARIFA);
        previa.rechazar();
        reservas.guardar(previa);

        assertEquals(EstadoReserva.PENDIENTE, service.crear(comando(HOY, HOY.plusDays(2))).estado());
    }

    @Test
    @DisplayName("Más de 30 días → ValorInvalidoException (400); exactamente 30 se permite")
    void duracionMaxima() {
        assertThrows(ValorInvalidoException.class, () -> service.crear(comando(HOY, HOY.plusDays(30))));
        assertEquals(30, service.crear(comando(HOY, HOY.plusDays(29))).dias());
    }

    @Test
    @DisplayName("Herramienta inexistente → HerramientaNoEncontradaException (404)")
    void herramientaInexistente() {
        CrearReservaCommand command = new CrearReservaCommand(CLIENTE, 404L, HOY, HOY);
        assertThrows(HerramientaNoEncontradaException.class, () -> service.crear(command));
    }

    @Test
    @DisplayName("Solo un CLIENTE puede reservar")
    void soloCliente() {
        CrearReservaCommand command = new CrearReservaCommand(new Solicitante(PROVEEDOR_ID, Rol.PROVEEDOR),
                HERRAMIENTA_ID, HOY, HOY);
        assertThrows(AccesoDenegadoException.class, () -> service.crear(command));
    }

    // ------------------------------------------------- límite de pendientes

    /** Guarda n reservas del CLIENTE en otras herramientas (no se cruzan) y les aplica la transición dada. */
    private void reservasPrevias(int n, Consumer<Reserva> transicion) {
        for (int i = 0; i < n; i++) {
            Reserva previa = Reserva.registrar(100L + i, CLIENTE.id(), new RangoFechas(HOY, HOY.plusDays(1)), TARIFA);
            transicion.accept(previa);
            reservas.guardar(previa);
        }
    }

    @Test
    @DisplayName("Con 2 reservas PENDIENTES el cliente aún puede crear otra")
    void dosPendientesDejaCrear() {
        reservasPrevias(2, r -> { });

        assertEquals(EstadoReserva.PENDIENTE, service.crear(comando(HOY, HOY.plusDays(1))).estado());
    }

    @Test
    @DisplayName("Con 3 reservas PENDIENTES → LimiteReservasPendientesException (409) con el límite en el mensaje")
    void tresPendientesRechaza() {
        reservasPrevias(3, r -> { });

        LimiteReservasPendientesException ex = assertThrows(LimiteReservasPendientesException.class,
                () -> service.crear(comando(HOY, HOY.plusDays(1))));
        assertTrue(ex.getMessage().contains("3"));
        assertEquals(3, reservas.guardadas.size(), "no se guardó la cuarta");
    }

    @Test
    @DisplayName("Con 3 reservas ACEPTADAS sí deja crear: solo cuentan las PENDIENTES")
    void tresAceptadasDejaCrear() {
        reservasPrevias(3, Reserva::aceptar);

        assertEquals(EstadoReserva.PENDIENTE, service.crear(comando(HOY, HOY.plusDays(1))).estado());
    }

    // ------------------------------------------------------- dobles de prueba

    /** Ejecuta la lambda tal cual y anota en el registro dónde empieza y termina. */
    private static final class UnidadDeTrabajoRegistrada implements UnidadDeTrabajo {

        private final List<String> registro;

        UnidadDeTrabajoRegistrada(List<String> registro) {
            this.registro = registro;
        }

        @Override
        public <T> T ejecutar(Supplier<T> operacion) {
            registro.add("inicio unidad");
            T resultado = operacion.get();
            registro.add("fin unidad");
            return resultado;
        }
    }

    /** Acumula los eventos publicados y anota cuántos llegaron en cada llamada. */
    private static final class PublicadorEnMemoria implements PublicadorEventos {

        private final List<String> registro;
        private final List<DomainEvent> publicados = new ArrayList<>();

        PublicadorEnMemoria(List<String> registro) {
            this.registro = registro;
        }

        @Override
        public void publicar(List<DomainEvent> eventos) {
            registro.add("publicar " + eventos.size());
            publicados.addAll(eventos);
        }
    }

    /**
     * Repositorio en memoria: asigna ids (o reemplaza si ya tiene) y resuelve solapamientos con las reglas
     * del dominio. Como el adaptador real, devuelve una copia reconstruida, sin los eventos del original.
     */
    private static final class ReservaRepositoryEnMemoria implements ReservaRepository {

        private final List<String> registro;
        private final List<Reserva> guardadas = new ArrayList<>();

        ReservaRepositoryEnMemoria(List<String> registro) {
            this.registro = registro;
        }

        @Override
        public Reserva guardar(Reserva reserva) {
            Long id = reserva.getId() != null ? reserva.getId() : (long) guardadas.size() + 1;
            Reserva conId = Reserva.reconstruir(id, reserva.getHerramientaId(),
                    reserva.getClienteId(), reserva.getRango(), reserva.getEstado(), reserva.getPrecioTotal(),
                    reserva.getFechaEntrega(), reserva.getFechaDevolucion(), reserva.getCreadoEn(),
                    reserva.getReportes());
            guardadas.removeIf(r -> r.getId().equals(id));
            guardadas.add(conId);
            registro.add("guardar");
            return conId;
        }

        @Override
        public boolean existeSolapamiento(Long herramientaId, RangoFechas rango) {
            return guardadas.stream().anyMatch(r -> r.getHerramientaId().equals(herramientaId)
                    && r.getEstado().bloqueaDisponibilidad()
                    && r.seCruzaCon(rango));
        }

        @Override
        public long contarPorClienteYEstado(Long clienteId, EstadoReserva estado) {
            return guardadas.stream()
                    .filter(r -> r.getClienteId().equals(clienteId) && r.getEstado() == estado)
                    .count();
        }

        @Override
        public Optional<Reserva> buscarPorId(Long id) {
            return guardadas.stream().filter(r -> r.getId().equals(id)).findFirst();
        }

        @Override
        public List<Reserva> buscarPorClienteId(Long clienteId) {
            throw new UnsupportedOperationException("no lo usa CrearReservaService");
        }

        @Override
        public List<Reserva> buscarPorHerramientaId(Long herramientaId) {
            throw new UnsupportedOperationException("no lo usa CrearReservaService");
        }

        @Override
        public List<Long> idsPorCliente(Long clienteId) {
            throw new UnsupportedOperationException("no lo usa CrearReservaService");
        }

        @Override
        public List<Long> idsPorHerramientas(Collection<Long> herramientaIds) {
            throw new UnsupportedOperationException("no lo usa CrearReservaService");
        }

        @Override
        public Pagina<Reserva> buscar(CriteriosBusquedaReserva criterios) {
            throw new UnsupportedOperationException("no lo usa CrearReservaService");
        }

        @Override
        public List<RangoFechas> rangosOcupados(Long herramientaId, RangoFechas periodo) {
            throw new UnsupportedOperationException("no lo usa CrearReservaService");
        }

        @Override
        public List<Reserva> buscarVencibles(LocalDate hoy) {
            throw new UnsupportedOperationException("no lo usa CrearReservaService");
        }
    }

    private static final class CatalogoEnMemoria implements CatalogoHerramientas {

        private final Map<Long, HerramientaReservable> herramientas = new HashMap<>();

        void agregar(HerramientaReservable herramienta) {
            herramientas.put(herramienta.id(), herramienta);
        }

        @Override
        public Optional<HerramientaReservable> buscar(Long herramientaId) {
            return Optional.ofNullable(herramientas.get(herramientaId));
        }

        @Override
        public List<Long> idsDelProveedor(Long proveedorId) {
            return herramientas.values().stream()
                    .filter(h -> h.proveedorId().equals(proveedorId))
                    .map(HerramientaReservable::id)
                    .toList();
        }

        @Override
        public void marcarDanada(Long herramientaId) {
            throw new UnsupportedOperationException("no lo usa CrearReservaService");
        }
    }
}
