package com.rentaherramientas.application.catalog.usecase;

import com.rentaherramientas.application.catalog.command.ActualizarHerramientaCommand;
import com.rentaherramientas.application.catalog.dto.HerramientaResponse;
import com.rentaherramientas.application.catalog.exception.CategoriaNoEncontradaException;
import com.rentaherramientas.domain.catalog.exception.HerramientaSinCategoriaException;
import com.rentaherramientas.domain.catalog.model.aggregate.Herramienta;
import com.rentaherramientas.domain.catalog.port.repository.CategoriaRepository;
import com.rentaherramientas.domain.catalog.port.repository.HerramientaRepository;
import com.rentaherramientas.domain.shared.model.valueobject.Dinero;

import java.util.Set;

/**
 * Actualiza una herramienta aplicando la regla de propiedad. Las categorías
 * nuevas se asignan ANTES de quitar las que sobran, para que el agregado nunca
 * quede (ni de paso) sin categoría.
 */
public class ActualizarHerramientaService implements ActualizarHerramientaUseCase {

    private final HerramientaRepository herramientaRepository;
    private final CategoriaRepository categoriaRepository;

    /** Recibe sus dependencias por constructor; el bean lo crea CatalogBeansConfig. */
    public ActualizarHerramientaService(HerramientaRepository herramientaRepository,
                                        CategoriaRepository categoriaRepository) {
        this.herramientaRepository = herramientaRepository;
        this.categoriaRepository = categoriaRepository;
    }

    /**
     * Actualiza datos, tarifa, descuento y categorías de una herramienta del proveedor (o de cualquiera si es ADMIN).
     * Primero añade las categorías nuevas y después quita las sobrantes, para que la herramienta
     * nunca se quede sin categoría a mitad del proceso.
     */
    @Override
    public HerramientaResponse actualizar(ActualizarHerramientaCommand command) {
        Herramienta herramienta = PropiedadHerramienta.cargarParaModificar(herramientaRepository,
                command.herramientaId(), command.solicitante());

        Set<Long> nuevasCategorias = command.categoriaIds();
        if (nuevasCategorias == null || nuevasCategorias.isEmpty() || nuevasCategorias.contains(null)) {
            throw new HerramientaSinCategoriaException();
        }
        if (!categoriaRepository.existenTodas(nuevasCategorias)) {
            throw new CategoriaNoEncontradaException(nuevasCategorias);
        }

        herramienta.actualizarDatos(command.nombre(), command.descripcion(), command.marca());
        herramienta.actualizarTarifa(Dinero.de(command.tarifaDiaria(),
                command.moneda() == null ? Dinero.MONEDA_POR_DEFECTO : command.moneda()));
        herramienta.definirDescuento(command.descuentoPorcentaje());
        nuevasCategorias.forEach(herramienta::asignarCategoria);
        herramienta.getCategoriaIds().stream()
                .filter(actual -> !nuevasCategorias.contains(actual))
                .forEach(herramienta::quitarCategoria);
        return HerramientaResponse.desde(herramientaRepository.guardar(herramienta));
    }
}
