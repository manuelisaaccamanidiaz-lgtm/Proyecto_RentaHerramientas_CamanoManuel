package com.rentaherramientas.application.catalog.usecase;

import com.rentaherramientas.application.catalog.dto.HerramientaResponse;
import com.rentaherramientas.application.catalog.exception.HerramientaNoEncontradaException;
import com.rentaherramientas.application.catalog.exception.HerramientaNoFavoritableException;
import com.rentaherramientas.application.shared.dto.PaginaResponse;
import com.rentaherramientas.domain.catalog.model.aggregate.Favorito;
import com.rentaherramientas.domain.catalog.model.aggregate.Herramienta;
import com.rentaherramientas.domain.catalog.model.valueobject.EstadoHerramienta;
import com.rentaherramientas.domain.catalog.port.repository.FavoritoRepository;
import com.rentaherramientas.domain.catalog.port.repository.HerramientaRepository;
import com.rentaherramientas.domain.shared.model.Pagina;
import com.rentaherramientas.domain.shared.model.SolicitudPagina;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Favoritos del cliente. Viven en catalog porque lo que se marca y se lista son
 * herramientas del catálogo: el caso de uso usa HerramientaRepository directamente
 * y no necesita un puerto anticorrupción (como CatalogoHerramientas en rental).
 * Del cliente (BC identity) solo hace falta su id, que llega del JWT, y la FK a
 * clientes garantiza en la base que existe.
 */
public class GestionarFavoritosService implements GestionarFavoritosUseCase {

    private final FavoritoRepository favoritoRepository;
    private final HerramientaRepository herramientaRepository;
    private final Clock clock;

    /** Recibe sus dependencias por constructor; el bean lo crea CatalogBeansConfig. */
    public GestionarFavoritosService(FavoritoRepository favoritoRepository,
                                     HerramientaRepository herramientaRepository, Clock clock) {
        this.favoritoRepository = favoritoRepository;
        this.herramientaRepository = herramientaRepository;
        this.clock = clock;
    }

    /**
     * Inexistente → 404; INACTIVA (dada de baja) → 409. Las herramientas en
     * mantenimiento o dañadas sí se pueden marcar: siguen en el catálogo público.
     */
    @Override
    public void marcar(Long clienteId, Long herramientaId) {
        Herramienta herramienta = herramientaRepository.buscarPorId(herramientaId)
                .orElseThrow(() -> new HerramientaNoEncontradaException(herramientaId));
        if (herramienta.getEstado() == EstadoHerramienta.INACTIVA) {
            throw new HerramientaNoFavoritableException(herramientaId);
        }
        favoritoRepository.agregar(new Favorito(clienteId, herramientaId, LocalDateTime.now(clock)));
    }

    /** No valida la herramienta: quitar una marca siempre es válido, aunque ya no exista o esté inactiva. */
    @Override
    public void desmarcar(Long clienteId, Long herramientaId) {
        favoritoRepository.quitar(clienteId, herramientaId);
    }

    /**
     * Pide la página de ids y luego carga esas herramientas de una vez (sin N+1),
     * conservando el orden de los favoritos. Incluye las que se inactivaron después
     * de marcarlas, para que el cliente vea su estado y pueda quitarlas.
     */
    @Override
    public PaginaResponse<HerramientaResponse> misFavoritos(Long clienteId, int pagina, int tamano) {
        Pagina<Long> ids = favoritoRepository.herramientaIdsDe(clienteId, new SolicitudPagina(pagina, tamano));
        Map<Long, Herramienta> porId = herramientaRepository.buscarPorIds(ids.contenido()).stream()
                .collect(Collectors.toMap(Herramienta::getId, Function.identity()));
        List<Herramienta> herramientas = ids.contenido().stream()
                .map(porId::get)
                .filter(Objects::nonNull)
                .toList();
        return PaginaResponse.desde(new Pagina<>(herramientas, ids.numero(), ids.tamano(), ids.totalElementos()),
                HerramientaResponse::desde);
    }
}
