package com.rentaherramientas.application.catalog.usecase;

import com.rentaherramientas.application.catalog.command.FiltroHerramientas;
import com.rentaherramientas.application.catalog.dto.HerramientaResponse;
import com.rentaherramientas.application.catalog.exception.HerramientaNoEncontradaException;
import com.rentaherramientas.application.shared.dto.PaginaResponse;
import com.rentaherramientas.domain.catalog.port.repository.CriteriosBusquedaHerramienta;
import com.rentaherramientas.domain.catalog.port.repository.HerramientaRepository;
import com.rentaherramientas.domain.shared.model.SolicitudPagina;

/**
 * Consultas de solo lectura sobre herramientas. Los límites de paginación los
 * valida SolicitudPagina (tamaño máximo 50).
 */
public class ConsultarHerramientaService implements ConsultarHerramientaUseCase {

    private final HerramientaRepository herramientaRepository;

    /** Recibe sus dependencias por constructor; el bean lo crea CatalogBeansConfig. */
    public ConsultarHerramientaService(HerramientaRepository herramientaRepository) {
        this.herramientaRepository = herramientaRepository;
    }

    /** Devuelve la herramienta o lanza HerramientaNoEncontradaException (404). */
    @Override
    public HerramientaResponse buscarPorId(Long id) {
        return herramientaRepository.buscarPorId(id)
                .map(HerramientaResponse::desde)
                .orElseThrow(() -> new HerramientaNoEncontradaException(id));
    }

    /** Traduce el filtro de la API a criterios del dominio y devuelve la página de resultados. */
    @Override
    public PaginaResponse<HerramientaResponse> buscar(FiltroHerramientas filtro) {
        CriteriosBusquedaHerramienta criterios = new CriteriosBusquedaHerramienta(
                filtro.categoriaId(),
                filtro.estado(),
                filtro.texto(),
                filtro.proveedorId(),
                filtro.marca(),
                filtro.precioMin(),
                filtro.precioMax(),
                filtro.soloCatalogoPublico(),
                new SolicitudPagina(filtro.pagina(), filtro.tamano()));
        return PaginaResponse.desde(herramientaRepository.buscar(criterios), HerramientaResponse::desde);
    }
}
