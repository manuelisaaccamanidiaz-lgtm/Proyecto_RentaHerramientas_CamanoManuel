package com.rentaherramientas.domain.rental.model.aggregate;

import com.rentaherramientas.domain.rental.exception.DanioNoPermitidoException;
import com.rentaherramientas.domain.rental.exception.FechaInvalidaException;
import com.rentaherramientas.domain.rental.exception.TransicionDeEstadoInvalidaException;
import com.rentaherramientas.domain.rental.model.valueobject.EstadoReserva;
import com.rentaherramientas.domain.rental.model.valueobject.RangoFechas;
import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;
import com.rentaherramientas.domain.shared.model.valueobject.Dinero;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservaTest {

    private static final RangoFechas RANGO = new RangoFechas(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 13));
    private static final Dinero TARIFA = Dinero.de(new BigDecimal("25000"), "COP");
    private static final LocalDateTime ENTREGA = LocalDateTime.of(2026, 11, 10, 9, 0);

    /** Acciones de la máquina de estados, con el nombre que se ve en el reporte de JUnit. */
    private static final Map<String, Consumer<Reserva>> ACCIONES = Map.of(
            "aceptar", Reserva::aceptar,
            "rechazar", Reserva::rechazar,
            "vencer", Reserva::vencer,
            "entregar", reserva -> reserva.registrarEntrega(ENTREGA),
            "devolver", reserva -> reserva.registrarDevolucion(ENTREGA.plusDays(3)));

    /** Únicas transiciones válidas (estado origen + acción); cualquier otra combinación debe fallar. */
    private static final Set<String> VALIDAS = Set.of(
            "PENDIENTE:aceptar", "PENDIENTE:rechazar", "PENDIENTE:vencer",
            "ACEPTADA:entregar", "ACEPTADA:vencer",
            "ENTREGADA:devolver");

    private static Reserva enEstado(EstadoReserva estado) {
        boolean entregada = estado == EstadoReserva.ENTREGADA || estado == EstadoReserva.DEVUELTA;
        return Reserva.reconstruir(1L, 7L, 3L, RANGO, estado, TARIFA.multiplicar(4),
                entregada ? ENTREGA : null,
                estado == EstadoReserva.DEVUELTA ? ENTREGA.plusDays(3) : null,
                LocalDateTime.of(2026, 11, 1, 8, 0), List.of());
    }

    // ---------------------------------------------------------------- alta

    @Test
    @DisplayName("registrar: empieza PENDIENTE y el precio es tarifa diaria × días")
    void registrarCalculaPrecio() {
        Reserva reserva = Reserva.registrar(7L, 3L, RANGO, TARIFA);

        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
        assertEquals(4, RANGO.dias());
        assertEquals(Dinero.de(new BigDecimal("100000"), "COP"), reserva.getPrecioTotal());
        assertNull(reserva.getFechaEntrega());
        assertTrue(reserva.getReportes().isEmpty());
    }

    @Test
    @DisplayName("registrar: una reserva de un solo día cuesta una tarifa")
    void registrarUnDia() {
        RangoFechas unDia = new RangoFechas(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 10));
        assertEquals(TARIFA, Reserva.registrar(7L, 3L, unDia, TARIFA).getPrecioTotal());
    }

    // -------------------------------------------------------- transiciones

    static Stream<Arguments> todasLasCombinaciones() {
        return Arrays.stream(EstadoReserva.values())
                .flatMap(estado -> ACCIONES.keySet().stream().sorted()
                        .map(accion -> Arguments.of(estado, accion, VALIDAS.contains(estado + ":" + accion))));
    }

    @ParameterizedTest(name = "{0} → {1}: válida = {2}")
    @MethodSource("todasLasCombinaciones")
    @DisplayName("Máquina de estados: todas las combinaciones estado × acción")
    void transiciones(EstadoReserva origen, String accion, boolean valida) {
        Reserva reserva = enEstado(origen);
        Consumer<Reserva> operacion = ACCIONES.get(accion);

        if (valida) {
            assertDoesNotThrow(() -> operacion.accept(reserva));
        } else {
            assertThrows(TransicionDeEstadoInvalidaException.class, () -> operacion.accept(reserva));
            assertEquals(origen, reserva.getEstado(), "una transición inválida no cambia el estado");
        }
    }

    @Test
    @DisplayName("Flujo completo PENDIENTE → ACEPTADA → ENTREGADA → DEVUELTA con sus fechas")
    void flujoCompleto() {
        Reserva reserva = enEstado(EstadoReserva.PENDIENTE);

        reserva.aceptar();
        assertEquals(EstadoReserva.ACEPTADA, reserva.getEstado());
        reserva.registrarEntrega(ENTREGA);
        assertEquals(EstadoReserva.ENTREGADA, reserva.getEstado());
        assertEquals(ENTREGA, reserva.getFechaEntrega());
        reserva.registrarDevolucion(ENTREGA.plusDays(3));
        assertEquals(EstadoReserva.DEVUELTA, reserva.getEstado());
        assertEquals(ENTREGA.plusDays(3), reserva.getFechaDevolucion());
    }

    @Test
    @DisplayName("Devolución sin entrega (ACEPTADA → DEVUELTA) es inválida")
    void devolucionSinEntrega() {
        Reserva reserva = enEstado(EstadoReserva.ACEPTADA);

        assertThrows(TransicionDeEstadoInvalidaException.class,
                () -> reserva.registrarDevolucion(ENTREGA));
        assertNull(reserva.getFechaDevolucion());
    }

    @Test
    @DisplayName("La devolución no puede ser anterior a la entrega")
    void devolucionAnteriorALaEntrega() {
        Reserva reserva = enEstado(EstadoReserva.ENTREGADA);

        assertThrows(FechaInvalidaException.class, () -> reserva.registrarDevolucion(ENTREGA.minusHours(1)));
        assertEquals(EstadoReserva.ENTREGADA, reserva.getEstado());
    }

    // --------------------------------------------------------------- daños

    @ParameterizedTest(name = "en {0}")
    @EnumSource(value = EstadoReserva.class, names = "DEVUELTA", mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("Reportar daño fuera de DEVUELTA lanza DanioNoPermitidoException")
    void danioSoloEnDevuelta(EstadoReserva estado) {
        Reserva reserva = enEstado(estado);

        assertThrows(DanioNoPermitidoException.class, () -> reserva.reportarDanio("Mango roto", null));
        assertTrue(reserva.getReportes().isEmpty());
    }

    @Test
    @DisplayName("Reportar daño en DEVUELTA lo agrega (costo opcional)")
    void danioEnDevuelta() {
        Reserva reserva = enEstado(EstadoReserva.DEVUELTA);

        reserva.reportarDanio("Mango roto", Dinero.de(new BigDecimal("15000"), "COP"));
        reserva.reportarDanio("Rayones sin cotizar", null);

        assertEquals(2, reserva.getReportes().size());
        ReporteDanio primero = reserva.getReportes().getFirst();
        assertEquals("Mango roto", primero.getDescripcion());
        assertNotNull(primero.getReportadoEn());
    }

    @Test
    @DisplayName("El costo del daño debe ir en la moneda de la reserva")
    void danioEnOtraMoneda() {
        Reserva reserva = enEstado(EstadoReserva.DEVUELTA);

        assertThrows(ValorInvalidoException.class,
                () -> reserva.reportarDanio("Mango roto", Dinero.de(new BigDecimal("10"), "USD")));
    }

    @Test
    @DisplayName("La lista de reportes es inmodificable desde fuera del agregado")
    void reportesInmodificables() {
        Reserva reserva = enEstado(EstadoReserva.DEVUELTA);
        reserva.reportarDanio("Mango roto", null);
        List<ReporteDanio> reportes = reserva.getReportes();
        ReporteDanio ajeno = ReporteDanio.reconstruir(99L, "Inyectado", null, LocalDateTime.now());

        assertThrows(UnsupportedOperationException.class, () -> reportes.add(ajeno));
        assertThrows(UnsupportedOperationException.class, reportes::clear);
        assertEquals(1, reserva.getReportes().size());
    }
}
