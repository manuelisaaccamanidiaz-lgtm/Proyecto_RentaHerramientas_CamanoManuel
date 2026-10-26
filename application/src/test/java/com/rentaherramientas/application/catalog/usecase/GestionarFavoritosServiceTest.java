package com.rentaherramientas.application.catalog.usecase;

import com.rentaherramientas.application.catalog.dto.HerramientaResponse;
import com.rentaherramientas.application.catalog.exception.HerramientaNoEncontradaException;
import com.rentaherramientas.application.catalog.exception.HerramientaNoFavoritableException;
import com.rentaherramientas.application.shared.dto.PaginaResponse;
import com.rentaherramientas.domain.catalog.model.aggregate.Favorito;
import com.rentaherramientas.domain.catalog.model.aggregate.Herramienta;
import com.rentaherramientas.domain.catalog.model.valueobject.EstadoHerramienta;
import com.rentaherramientas.domain.catalog.port.repository.CriteriosBusquedaHerramienta;
import com.rentaherramientas.domain.catalog.port.repository.FavoritoRepository;
import com.rentaherramientas.domain.catalog.port.repository.HerramientaRepository;
import com.rentaherramientas.domain.shared.model.Pagina;
import com.rentaherramientas.domain.shared.model.SolicitudPagina;
import com.rentaherramientas.domain.shared.model.valueobject.Dinero;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del caso de uso con dobles escritos a mano (sin Mockito ni Spring): un
 * repositorio de herramientas y otro de favoritos en memoria. El de favoritos
 * imita la PK compuesta (cliente, herramienta) de la tabla real.
 */
class GestionarFavoritosServiceTest {

    private static final Long CLIENTE_ID = 30L;
    private static final Long DISPONIBLE_ID = 1L;
    private static final Long INACTIVA_ID = 2L;
    private static final Long MANTENIMIENTO_ID = 3L;
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-10-22T15:00:00Z"), ZoneId.of("America/Bogota"));

    private FavoritoRepositoryEnMemoria favoritos;
    private GestionarFavoritosService service;

    @BeforeEach
    void preparar() {
        HerramientaRepositoryEnMemoria herramientas = new HerramientaRepositoryEnMemoria();
        herramientas.agregar(herramienta(DISPONIBLE_ID, EstadoHerramienta.DISPONIBLE));
        herramientas.agregar(herramienta(INACTIVA_ID, EstadoHerramienta.INACTIVA));
        herramientas.agregar(herramienta(MANTENIMIENTO_ID, EstadoHerramienta.EN_MANTENIMIENTO));
        favoritos = new FavoritoRepositoryEnMemoria();
        service = new GestionarFavoritosService(favoritos, herramientas, RELOJ);
    }

    @Test
    @DisplayName("Marcar dos veces la misma herramienta deja un solo favorito")
    void marcarEsIdempotente() {
        service.marcar(CLIENTE_ID, DISPONIBLE_ID);
        service.marcar(CLIENTE_ID, DISPONIBLE_ID);

        PaginaResponse<HerramientaResponse> pagina = service.misFavoritos(CLIENTE_ID, 0, 20);
        assertEquals(1, pagina.totalElementos());
        assertEquals(DISPONIBLE_ID, pagina.contenido().get(0).id());
    }

    @Test
    @DisplayName("Desmarcar una herramienta que no era favorita (o que no existe) no falla")
    void desmarcarInexistenteNoFalla() {
        assertDoesNotThrow(() -> service.desmarcar(CLIENTE_ID, DISPONIBLE_ID));
        assertDoesNotThrow(() -> service.desmarcar(CLIENTE_ID, 999L));
        assertTrue(favoritos.porClave.isEmpty());
    }

    @Test
    @DisplayName("Marcar una herramienta INACTIVA → HerramientaNoFavoritableException (409) y no guarda")
    void herramientaInactiva() {
        assertThrows(HerramientaNoFavoritableException.class, () -> service.marcar(CLIENTE_ID, INACTIVA_ID));
        assertTrue(favoritos.porClave.isEmpty());
    }

    @Test
    @DisplayName("Marcar una herramienta que no existe → HerramientaNoEncontradaException (404)")
    void herramientaInexistente() {
        assertThrows(HerramientaNoEncontradaException.class, () -> service.marcar(CLIENTE_ID, 999L));
        assertTrue(favoritos.porClave.isEmpty());
    }

    @Test
    @DisplayName("Una herramienta en mantenimiento sí se puede marcar; la lista va de la más reciente a la más antigua")
    void listaOrdenada() {
        service.marcar(CLIENTE_ID, DISPONIBLE_ID);
        service.marcar(CLIENTE_ID, MANTENIMIENTO_ID);
        service.marcar(31L, DISPONIBLE_ID); // favorito de otro cliente: no debe aparecer

        PaginaResponse<HerramientaResponse> pagina = service.misFavoritos(CLIENTE_ID, 0, 20);

        assertEquals(List.of(MANTENIMIENTO_ID, DISPONIBLE_ID),
                pagina.contenido().stream().map(HerramientaResponse::id).toList());
    }

    private static Herramienta herramienta(Long id, EstadoHerramienta estado) {
        return Herramienta.reconstruir(id, 10L, "Herramienta " + id, null, null,
                Dinero.de(new BigDecimal("20000"), "COP"), null, estado, List.of(), Set.of(1L));
    }

    // ------------------------------------------------------- dobles de prueba

    /** Favoritos en memoria; la clave "cliente-herramienta" hace de PK compuesta. */
    private static final class FavoritoRepositoryEnMemoria implements FavoritoRepository {

        /** LinkedHashMap: conserva el orden en que se marcaron. */
        private final Map<String, Favorito> porClave = new LinkedHashMap<>();

        private static String clave(Long clienteId, Long herramientaId) {
            return clienteId + "-" + herramientaId;
        }

        @Override
        public void agregar(Favorito favorito) {
            porClave.putIfAbsent(clave(favorito.clienteId(), favorito.herramientaId()), favorito);
        }

        @Override
        public void quitar(Long clienteId, Long herramientaId) {
            porClave.remove(clave(clienteId, herramientaId));
        }

        @Override
        public Pagina<Long> herramientaIdsDe(Long clienteId, SolicitudPagina pagina) {
            List<Long> ids = new ArrayList<>(porClave.values().stream()
                    .filter(f -> f.clienteId().equals(clienteId))
                    .map(Favorito::herramientaId)
                    .toList());
            Collections.reverse(ids); // el más reciente primero, como ORDER BY creado_en DESC
            return new Pagina<>(ids, pagina.numero(), pagina.tamano(), ids.size());
        }
    }

    /** Herramientas en memoria; solo implementa de verdad lo que usa el caso de uso. */
    private static final class HerramientaRepositoryEnMemoria implements HerramientaRepository {

        private final Map<Long, Herramienta> porId = new HashMap<>();

        void agregar(Herramienta herramienta) {
            porId.put(herramienta.getId(), herramienta);
        }

        @Override
        public Herramienta guardar(Herramienta herramienta) {
            porId.put(herramienta.getId(), herramienta);
            return herramienta;
        }

        @Override
        public Optional<Herramienta> buscarPorId(Long id) {
            return Optional.ofNullable(porId.get(id));
        }

        @Override
        public List<Herramienta> buscarPorIds(Collection<Long> ids) {
            return ids.stream().map(porId::get).filter(Objects::nonNull).toList();
        }

        @Override
        public List<Herramienta> buscarPorProveedorId(Long proveedorId) {
            return porId.values().stream().filter(h -> h.getProveedorId().equals(proveedorId)).toList();
        }

        @Override
        public List<Long> idsPorProveedor(Long proveedorId) {
            return buscarPorProveedorId(proveedorId).stream().map(Herramienta::getId).toList();
        }

        @Override
        public List<Herramienta> buscarPorEstado(EstadoHerramienta estado) {
            return porId.values().stream().filter(h -> h.getEstado() == estado).toList();
        }

        @Override
        public Pagina<Herramienta> buscar(CriteriosBusquedaHerramienta criterios) {
            throw new UnsupportedOperationException("No lo usa GestionarFavoritosService");
        }
    }
}
