package com.rentaherramientas.application.billing.usecase;

import com.rentaherramientas.application.billing.command.PagarReservaCommand;
import com.rentaherramientas.application.billing.dto.PagoResponse;
import com.rentaherramientas.application.billing.exception.ReservaNoEncontradaException;
import com.rentaherramientas.application.billing.exception.ReservaNoPagableException;
import com.rentaherramientas.application.billing.port.ConsultaReservas;
import com.rentaherramientas.application.billing.port.GeneradorNumeroFactura;
import com.rentaherramientas.application.billing.port.ReservaFacturable;
import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.application.shared.exception.AccesoDenegadoException;
import com.rentaherramientas.application.shared.port.PublicadorEventos;
import com.rentaherramientas.application.shared.port.UnidadDeTrabajo;
import com.rentaherramientas.domain.billing.event.PagoAprobado;
import com.rentaherramientas.domain.billing.exception.PagoDuplicadoException;
import com.rentaherramientas.domain.billing.model.aggregate.Factura;
import com.rentaherramientas.domain.billing.model.aggregate.LineaFactura;
import com.rentaherramientas.domain.billing.model.aggregate.Pago;
import com.rentaherramientas.domain.billing.model.valueobject.EstadoPago;
import com.rentaherramientas.domain.billing.model.valueobject.MetodoPago;
import com.rentaherramientas.domain.billing.model.valueobject.NumeroFactura;
import com.rentaherramientas.domain.billing.model.valueobject.ReferenciaPago;
import com.rentaherramientas.domain.billing.port.repository.CriteriosBusquedaPago;
import com.rentaherramientas.domain.billing.port.repository.FacturaRepository;
import com.rentaherramientas.domain.billing.port.repository.PagoRepository;
import com.rentaherramientas.domain.identity.model.valueobject.Rol;
import com.rentaherramientas.domain.shared.event.DomainEvent;
import com.rentaherramientas.domain.shared.model.Pagina;
import com.rentaherramientas.domain.shared.model.SolicitudPagina;
import com.rentaherramientas.domain.shared.model.valueobject.Dinero;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

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
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de PagarReservaService con dobles escritos a mano (sin Mockito ni
 * Spring): repositorios en memoria, catálogo de reservas en memoria, generador
 * de números secuencial, unidad de trabajo que registra qué se guardó dentro de
 * ella, un publicador que acumula los eventos y un reloj fijo.
 */
class PagarReservaServiceTest {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    private static final Clock RELOJ = Clock.fixed(
            LocalDateTime.of(2026, 10, 15, 10, 30).atZone(BOGOTA).toInstant(), BOGOTA);

    private static final Long CLIENTE_ID = 5L;
    private static final Long PROVEEDOR_ID = 2L;
    private static final Long RESERVA_ID = 40L;
    private static final Solicitante CLIENTE = new Solicitante(CLIENTE_ID, Rol.CLIENTE);
    /** 3 días × 25.000 = 75.000 COP. */
    private static final Dinero PRECIO = Dinero.de(new BigDecimal("75000"), "COP");

    private PagoRepositoryEnMemoria pagos;
    private FacturaRepositoryEnMemoria facturas;
    private ConsultaReservasEnMemoria reservas;
    private UnidadDeTrabajoRegistrada unidadDeTrabajo;
    private PublicadorEnMemoria publicador;
    private PagarReservaService service;

    @BeforeEach
    void preparar() {
        unidadDeTrabajo = new UnidadDeTrabajoRegistrada();
        pagos = new PagoRepositoryEnMemoria(unidadDeTrabajo);
        facturas = new FacturaRepositoryEnMemoria(unidadDeTrabajo);
        reservas = new ConsultaReservasEnMemoria();
        reservas.agregar(reserva(RESERVA_ID, "ACEPTADA"));
        publicador = new PublicadorEnMemoria(unidadDeTrabajo, pagos);
        service = new PagarReservaService(pagos, facturas, reservas, new GeneradorSecuencial(), unidadDeTrabajo,
                publicador, RELOJ);
    }

    private static ReservaFacturable reserva(Long id, String estado) {
        return new ReservaFacturable(id, CLIENTE_ID, PROVEEDOR_ID, "Taladro percutor Bosch",
                LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 22), 3, PRECIO, estado);
    }

    private PagoResponse pagar(MetodoPago metodo) {
        return service.pagar(new PagarReservaCommand(CLIENTE, RESERVA_ID, metodo));
    }

    // ------------------------------------------------------- reglas de cobro

    @ParameterizedTest(name = "reserva {0}")
    @EnumSource(EstadoReservaPrueba.class)
    @DisplayName("Reserva no ACEPTADA → ReservaNoPagableException (409) y no se guarda nada")
    void reservaNoAceptada(EstadoReservaPrueba estado) {
        reservas.agregar(reserva(RESERVA_ID, estado.name()));

        assertThrows(ReservaNoPagableException.class, () -> pagar(MetodoPago.TARJETA));
        assertTrue(pagos.guardados.isEmpty());
        assertTrue(facturas.guardadas.isEmpty());
    }

    @Test
    @DisplayName("Pago doble: con un pago APROBADO, el segundo → PagoDuplicadoException (409)")
    void pagoDoble() {
        pagar(MetodoPago.TARJETA);

        assertThrows(PagoDuplicadoException.class, () -> pagar(MetodoPago.PSE));
        assertEquals(1, pagos.guardados.size());
        assertEquals(1, facturas.guardadas.size());
    }

    @Test
    @DisplayName("Un pago en EFECTIVO PENDIENTE también impide un segundo pago")
    void pagoDobleConEfectivoPendiente() {
        pagar(MetodoPago.EFECTIVO);

        assertThrows(PagoDuplicadoException.class, () -> pagar(MetodoPago.TARJETA));
    }

    @Test
    @DisplayName("Tras un pago RECHAZADO la reserva se puede volver a pagar")
    void reintentoTrasRechazo() {
        Pago rechazado = Pago.registrar(RESERVA_ID, PRECIO, MetodoPago.EFECTIVO, new ReferenciaPago("PAG-OLD-1"),
                LocalDateTime.now(RELOJ));
        rechazado.rechazar();
        pagos.guardar(rechazado);

        assertEquals(EstadoPago.APROBADO, pagar(MetodoPago.TARJETA).estado());
    }

    @Test
    @DisplayName("El monto lo calcula el servidor: es el precio total de la reserva")
    void montoCalculadoPorElServidor() {
        PagoResponse respuesta = pagar(MetodoPago.TARJETA);

        assertEquals(new BigDecimal("75000.00"), respuesta.monto());
        assertEquals("COP", respuesta.moneda());
        assertEquals(PRECIO, pagos.guardados.getFirst().getMonto());
    }

    @Test
    @DisplayName("La referencia tiene el formato PAG-<año>-<12 caracteres>")
    void referenciaGenerada() {
        assertTrue(pagar(MetodoPago.PSE).referencia().matches("^PAG-2026-[0-9A-F]{12}$"));
    }

    // -------------------------------------------------------------- factura

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = MetodoPago.class, names = {"TARJETA", "PSE"})
    @DisplayName("TARJETA/PSE: se aprueba y se emite la factura en la misma unidad de trabajo")
    void facturaEmitidaAlAprobar(MetodoPago metodo) {
        PagoResponse respuesta = pagar(metodo);

        assertEquals(EstadoPago.APROBADO, respuesta.estado());
        assertNotNull(respuesta.factura());
        assertEquals("FAC-2026-000001", respuesta.factura().numero());

        Factura factura = facturas.guardadas.getFirst();
        Pago pago = pagos.guardados.getFirst();
        assertEquals(pago.getId(), factura.getPagoId());
        assertEquals(pago.getMonto(), factura.total(), "el total de la factura es el monto del pago");
        assertEquals(LocalDateTime.now(RELOJ), factura.getFechaEmision());

        LineaFactura linea = factura.getLineas().getFirst();
        assertEquals(1, factura.getLineas().size());
        assertEquals("Alquiler de Taladro percutor Bosch del 20/10/2026 al 22/10/2026", linea.getDescripcion());
        assertEquals(3, linea.getCantidad());
        assertEquals(Dinero.de(new BigDecimal("25000"), "COP"), linea.getValorUnitario());

        assertEquals(1, unidadDeTrabajo.ejecuciones);
        assertTrue(pagos.guardadoDentroDeUnidad && facturas.guardadaDentroDeUnidad,
                "pago y factura se guardan dentro de la misma unidad de trabajo");
    }

    @Test
    @DisplayName("EFECTIVO: queda PENDIENTE y no se emite factura")
    void efectivoQuedaPendiente() {
        PagoResponse respuesta = pagar(MetodoPago.EFECTIVO);

        assertEquals(EstadoPago.PENDIENTE, respuesta.estado());
        assertNull(respuesta.factura());
        assertTrue(facturas.guardadas.isEmpty());
    }

    // -------------------------------------------------------------- eventos

    @Test
    @DisplayName("TARJETA: publica exactamente un PagoAprobado, tras guardar el pago y dentro de la unidad de trabajo")
    void publicaPagoAprobado() {
        pagar(MetodoPago.TARJETA);

        assertEquals(1, publicador.publicados.size());
        PagoAprobado evento = assertInstanceOf(PagoAprobado.class, publicador.publicados.getFirst());
        assertEquals(RESERVA_ID, evento.reservaId());
        assertEquals(PRECIO, evento.monto());
        assertTrue(publicador.publicadoDentroDeUnidad, "se publica dentro de la unidad de trabajo");
        assertTrue(publicador.pagoYaGuardado, "se publica después de guardar el pago");
    }

    @Test
    @DisplayName("EFECTIVO: el pago queda PENDIENTE y no se publica ningún evento")
    void efectivoNoPublica() {
        pagar(MetodoPago.EFECTIVO);

        assertTrue(publicador.publicados.isEmpty());
    }

    // --------------------------------------------------------------- acceso

    @Test
    @DisplayName("Otro cliente no puede pagar la reserva ajena (403)")
    void otroCliente() {
        PagarReservaCommand command = new PagarReservaCommand(new Solicitante(99L, Rol.CLIENTE), RESERVA_ID,
                MetodoPago.TARJETA);
        assertThrows(AccesoDenegadoException.class, () -> service.pagar(command));
    }

    @Test
    @DisplayName("Solo un CLIENTE paga reservas (403 para PROVEEDOR)")
    void soloCliente() {
        PagarReservaCommand command = new PagarReservaCommand(new Solicitante(PROVEEDOR_ID, Rol.PROVEEDOR),
                RESERVA_ID, MetodoPago.TARJETA);
        assertThrows(AccesoDenegadoException.class, () -> service.pagar(command));
    }

    @Test
    @DisplayName("Reserva inexistente → 404")
    void reservaInexistente() {
        PagarReservaCommand command = new PagarReservaCommand(CLIENTE, 404L, MetodoPago.TARJETA);
        assertThrows(ReservaNoEncontradaException.class, () -> service.pagar(command));
    }

    // ------------------------------------------------------- dobles de prueba

    /** Estados de rental como texto, igual que los ve billing a través del puerto. */
    enum EstadoReservaPrueba { PENDIENTE, RECHAZADA, ENTREGADA, DEVUELTA, VENCIDA }

    /** Ejecuta la operación y deja constancia de que se está dentro de la unidad de trabajo. */
    private static final class UnidadDeTrabajoRegistrada implements UnidadDeTrabajo {

        private int ejecuciones;
        private boolean dentro;

        @Override
        public <T> T ejecutar(Supplier<T> operacion) {
            ejecuciones++;
            dentro = true;
            try {
                return operacion.get();
            } finally {
                dentro = false;
            }
        }
    }

    /** Acumula los eventos y anota si llegaron dentro de la unidad y con el pago ya guardado. */
    private static final class PublicadorEnMemoria implements PublicadorEventos {

        private final UnidadDeTrabajoRegistrada unidad;
        private final PagoRepositoryEnMemoria pagos;
        private final List<DomainEvent> publicados = new ArrayList<>();
        private boolean publicadoDentroDeUnidad;
        private boolean pagoYaGuardado;

        PublicadorEnMemoria(UnidadDeTrabajoRegistrada unidad, PagoRepositoryEnMemoria pagos) {
            this.unidad = unidad;
            this.pagos = pagos;
        }

        @Override
        public void publicar(List<DomainEvent> eventos) {
            publicadoDentroDeUnidad = unidad.dentro;
            pagoYaGuardado = !pagos.guardados.isEmpty();
            publicados.addAll(eventos);
        }
    }

    private static final class PagoRepositoryEnMemoria implements PagoRepository {

        private final UnidadDeTrabajoRegistrada unidad;
        private final List<Pago> guardados = new ArrayList<>();
        private boolean guardadoDentroDeUnidad;

        PagoRepositoryEnMemoria(UnidadDeTrabajoRegistrada unidad) {
            this.unidad = unidad;
        }

        @Override
        public Pago guardar(Pago pago) {
            guardadoDentroDeUnidad = unidad.dentro;
            Pago conId = Pago.reconstruir((long) guardados.size() + 1, pago.getReservaId(), pago.getMonto(),
                    pago.getMetodo(), pago.getEstado(), pago.getReferencia(), pago.getFecha());
            guardados.add(conId);
            return conId;
        }

        @Override
        public boolean existePagoVigente(Long reservaId) {
            return guardados.stream().anyMatch(p -> p.getReservaId().equals(reservaId)
                    && p.getEstado() != EstadoPago.RECHAZADO);
        }

        @Override
        public Optional<Pago> buscarPorId(Long id) {
            return guardados.stream().filter(p -> p.getId().equals(id)).findFirst();
        }

        @Override
        public Optional<Pago> buscarPorReferencia(ReferenciaPago referencia) {
            throw new UnsupportedOperationException("no lo usa PagarReservaService");
        }

        @Override
        public List<Pago> buscarPorReservaId(Long reservaId) {
            throw new UnsupportedOperationException("no lo usa PagarReservaService");
        }

        @Override
        public boolean existePagoAprobado(Long reservaId) {
            throw new UnsupportedOperationException("no lo usa PagarReservaService");
        }

        @Override
        public Set<Long> reservasConPagoAprobado(Collection<Long> reservaIds) {
            throw new UnsupportedOperationException("no lo usa PagarReservaService");
        }

        @Override
        public Pagina<Pago> buscar(CriteriosBusquedaPago criterios) {
            throw new UnsupportedOperationException("no lo usa PagarReservaService");
        }
    }

    private static final class FacturaRepositoryEnMemoria implements FacturaRepository {

        private final UnidadDeTrabajoRegistrada unidad;
        private final List<Factura> guardadas = new ArrayList<>();
        private boolean guardadaDentroDeUnidad;

        FacturaRepositoryEnMemoria(UnidadDeTrabajoRegistrada unidad) {
            this.unidad = unidad;
        }

        @Override
        public Factura guardar(Factura factura) {
            guardadaDentroDeUnidad = unidad.dentro;
            Factura conId = Factura.reconstruir((long) guardadas.size() + 1, factura.getNumero(),
                    factura.getPagoId(), factura.getFechaEmision(),
                    factura.getLineas().stream()
                            .map(l -> LineaFactura.reconstruir(1L, l.getDescripcion(), l.getCantidad(),
                                    l.getValorUnitario()))
                            .toList());
            guardadas.add(conId);
            return conId;
        }

        @Override
        public Optional<Factura> buscarPorId(Long id) {
            throw new UnsupportedOperationException("no lo usa PagarReservaService");
        }

        @Override
        public Optional<Factura> buscarPorNumero(NumeroFactura numero) {
            throw new UnsupportedOperationException("no lo usa PagarReservaService");
        }

        @Override
        public boolean existePorPagoId(Long pagoId) {
            throw new UnsupportedOperationException("no lo usa PagarReservaService");
        }

        @Override
        public Optional<Factura> buscarPorPagoId(Long pagoId) {
            throw new UnsupportedOperationException("no lo usa PagarReservaService");
        }

        @Override
        public List<Factura> buscarPorPagoIds(Collection<Long> pagoIds) {
            throw new UnsupportedOperationException("no lo usa PagarReservaService");
        }

        @Override
        public Pagina<Factura> buscar(Collection<Long> reservaIds, SolicitudPagina pagina) {
            throw new UnsupportedOperationException("no lo usa PagarReservaService");
        }
    }

    private static final class ConsultaReservasEnMemoria implements ConsultaReservas {

        private final Map<Long, ReservaFacturable> reservas = new HashMap<>();

        void agregar(ReservaFacturable reserva) {
            reservas.put(reserva.reservaId(), reserva);
        }

        @Override
        public Optional<ReservaFacturable> buscar(Long reservaId) {
            return Optional.ofNullable(reservas.get(reservaId));
        }

        @Override
        public List<Long> reservaIdsDelCliente(Long clienteId) {
            throw new UnsupportedOperationException("no lo usa PagarReservaService");
        }

        @Override
        public List<Long> reservaIdsDelProveedor(Long proveedorId) {
            throw new UnsupportedOperationException("no lo usa PagarReservaService");
        }
    }

    private static final class GeneradorSecuencial implements GeneradorNumeroFactura {

        private int siguiente = 1;

        @Override
        public NumeroFactura siguiente(int anio) {
            return new NumeroFactura("FAC-%d-%06d".formatted(anio, siguiente++));
        }
    }
}
