package com.rentaherramientas.infrastructure.rental.adapters.out.catalog;

import com.rentaherramientas.application.rental.port.CatalogoHerramientas;
import com.rentaherramientas.application.rental.port.HerramientaReservable;
import com.rentaherramientas.domain.catalog.model.valueobject.EstadoHerramienta;
import com.rentaherramientas.domain.catalog.port.repository.HerramientaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador anticorrupción: implementa el puerto de rental con el repositorio
 * del BC catalog. Es el único punto donde rental "toca" catalog, y traduce su
 * agregado Herramienta al modelo que rental necesita (HerramientaReservable).
 */
@Component
public class CatalogoHerramientasAdapter implements CatalogoHerramientas {

    private final HerramientaRepository herramientaRepository;

    /** Recibe el repositorio de herramientas de catalog por constructor. */
    public CatalogoHerramientasAdapter(HerramientaRepository herramientaRepository) {
        this.herramientaRepository = herramientaRepository;
    }

    /** Traduce la herramienta de catalog a los datos mínimos que necesita rental. */
    @Override
    public Optional<HerramientaReservable> buscar(Long herramientaId) {
        return herramientaRepository.buscarPorId(herramientaId)
                .map(herramienta -> new HerramientaReservable(
                        herramienta.getId(),
                        herramienta.getProveedorId(),
                        herramienta.getTarifaDiaria(),
                        herramienta.estaDisponible()));
    }

    /** Ids de las herramientas del proveedor. */
    @Override
    public List<Long> idsDelProveedor(Long proveedorId) {
        return herramientaRepository.idsPorProveedor(proveedorId);
    }

    /**
     * Las transiciones las decide el agregado de catalog: si la herramienta ya
     * está DANADA o fue dada de baja (INACTIVA), el reporte de daño no la mueve.
     */
    @Override
    public void marcarDanada(Long herramientaId) {
        herramientaRepository.buscarPorId(herramientaId)
                .filter(herramienta -> herramienta.getEstado().puedeCambiarA(EstadoHerramienta.DANADA))
                .ifPresent(herramienta -> {
                    herramienta.marcarDanada();
                    herramientaRepository.guardar(herramienta);
                });
    }
}
