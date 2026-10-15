package com.rentaherramientas.application.rental.usecase;

import com.rentaherramientas.application.rental.command.CrearReservaCommand;
import com.rentaherramientas.application.rental.dto.ReservaResponse;
import com.rentaherramientas.application.rental.exception.HerramientaNoDisponibleException;
import com.rentaherramientas.application.rental.exception.HerramientaNoEncontradaException;
import com.rentaherramientas.application.rental.port.CatalogoHerramientas;
import com.rentaherramientas.application.rental.port.HerramientaReservable;
import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.application.shared.exception.AccesoDenegadoException;
import com.rentaherramientas.domain.identity.model.valueobject.Rol;
import com.rentaherramientas.domain.rental.exception.ReservaSolapadaException;
import com.rentaherramientas.domain.rental.model.aggregate.Reserva;
import com.rentaherramientas.domain.rental.model.valueobject.EstadoReserva;
import com.rentaherramientas.domain.rental.model.valueobject.RangoFechas;
import com.rentaherramientas.domain.rental.port.repository.CriteriosBusquedaReserva;
import com.rentaherramientas.domain.rental.port.repository.ReservaRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del caso de uso con dobles escritos a mano (sin Mockito ni Spring):
 * un repositorio en memoria, un catálogo en memoria y un reloj fijo.
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

    private ReservaRepositoryEnMemoria reservas;
    private CatalogoEnMemoria catalogo;
    private CrearReservaService service;

    @BeforeEach
    void preparar() {
        reservas = new ReservaRepositoryEnMemoria();
        catalogo = new CatalogoEnMemoria();
        catalogo.agregar(new HerramientaReservable(HERRAMIENTA_ID, PROVEEDOR_ID, TARIFA, true));
        service = new CrearReservaService(reservas, catalogo, RELOJ);
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

    // ------------------------------------------------------- dobles de prueba

    /** Repositorio en memoria: asigna ids y resuelve solapamientos con las reglas del dominio. */
    private static final class ReservaRepositoryEnMemoria implements ReservaRepository {

        private final List<Reserva> guardadas = new ArrayList<>();

        @Override
        public Reserva guardar(Reserva reserva) {
            Reserva conId = Reserva.reconstruir((long) guardadas.size() + 1, reserva.getHerramientaId(),
                    reserva.getClienteId(), reserva.getRango(), reserva.getEstado(), reserva.getPrecioTotal(),
                    reserva.getFechaEntrega(), reserva.getFechaDevolucion(), reserva.getCreadoEn(),
                    reserva.getReportes());
            guardadas.add(conId);
            return conId;
        }

        @Override
        public boolean existeSolapamiento(Long herramientaId, RangoFechas rango) {
            return guardadas.stream().anyMatch(r -> r.getHerramientaId().equals(herramientaId)
                    && r.getEstado().bloqueaDisponibilidad()
                    && r.seCruzaCon(rango));
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
