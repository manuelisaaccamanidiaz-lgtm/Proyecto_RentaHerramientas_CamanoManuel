package com.rentaherramientas.application.catalog.usecase;

import com.rentaherramientas.application.catalog.command.CambiarEstadoHerramientaCommand;
import com.rentaherramientas.application.catalog.dto.HerramientaResponse;
import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.domain.catalog.model.aggregate.Herramienta;
import com.rentaherramientas.domain.catalog.model.valueobject.EstadoHerramienta;
import com.rentaherramientas.domain.catalog.port.repository.HerramientaRepository;
import com.rentaherramientas.domain.shared.model.Validaciones;

/**
 * Traduce el estado pedido al método de negocio del agregado, que valida la
 * transición. Eliminar es idempotente: borrar una herramienta ya inactiva no falla.
 */
public class CambiarEstadoHerramientaService implements CambiarEstadoHerramientaUseCase {

    private final HerramientaRepository herramientaRepository;

    public CambiarEstadoHerramientaService(HerramientaRepository herramientaRepository) {
        this.herramientaRepository = herramientaRepository;
    }

    @Override
    public HerramientaResponse cambiarEstado(CambiarEstadoHerramientaCommand command) {
        EstadoHerramienta destino = Validaciones.requerido(command.estado(), "estado");
        Herramienta herramienta = PropiedadHerramienta.cargarParaModificar(herramientaRepository,
                command.herramientaId(), command.solicitante());
        switch (destino) {
            case DISPONIBLE -> herramienta.marcarDisponible();
            case EN_MANTENIMIENTO -> herramienta.marcarEnMantenimiento();
            case DANADA -> herramienta.marcarDanada();
            case INACTIVA -> herramienta.inactivar();
        }
        return HerramientaResponse.desde(herramientaRepository.guardar(herramienta));
    }

    @Override
    public void eliminar(Long herramientaId, Solicitante solicitante) {
        Herramienta herramienta = PropiedadHerramienta.cargarParaModificar(herramientaRepository,
                herramientaId, solicitante);
        if (herramienta.getEstado() != EstadoHerramienta.INACTIVA) {
            herramienta.inactivar();
            herramientaRepository.guardar(herramienta);
        }
    }
}
