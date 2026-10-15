package com.rentaherramientas.application.catalog.usecase;

import com.rentaherramientas.application.catalog.command.RegistrarHerramientaCommand;
import com.rentaherramientas.application.catalog.dto.HerramientaResponse;
import com.rentaherramientas.application.catalog.exception.CategoriaNoEncontradaException;
import com.rentaherramientas.application.shared.exception.AccesoDenegadoException;
import com.rentaherramientas.domain.catalog.model.aggregate.Herramienta;
import com.rentaherramientas.domain.catalog.port.repository.CategoriaRepository;
import com.rentaherramientas.domain.catalog.port.repository.HerramientaRepository;
import com.rentaherramientas.domain.identity.model.valueobject.Rol;
import com.rentaherramientas.domain.shared.model.valueobject.Dinero;

/**
 * Registra una herramienta a nombre del solicitante, que debe ser PROVEEDOR
 * (SecurityConfig ya lo filtra por URL; aquí se repite porque es regla de
 * negocio). Las categorías deben existir antes de asignarlas.
 */
public class RegistrarHerramientaService implements RegistrarHerramientaUseCase {

    private final HerramientaRepository herramientaRepository;
    private final CategoriaRepository categoriaRepository;

    public RegistrarHerramientaService(HerramientaRepository herramientaRepository,
                                       CategoriaRepository categoriaRepository) {
        this.herramientaRepository = herramientaRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public HerramientaResponse registrar(RegistrarHerramientaCommand command) {
        if (command.solicitante().rol() != Rol.PROVEEDOR) {
            throw new AccesoDenegadoException("Solo un PROVEEDOR puede registrar herramientas");
        }
        Dinero tarifa = Dinero.de(command.tarifaDiaria(),
                command.moneda() == null ? Dinero.MONEDA_POR_DEFECTO : command.moneda());
        Herramienta herramienta = Herramienta.registrar(command.solicitante().id(), command.nombre(),
                command.descripcion(), tarifa, command.categoriaIds());
        if (!categoriaRepository.existenTodas(herramienta.getCategoriaIds())) {
            throw new CategoriaNoEncontradaException(herramienta.getCategoriaIds());
        }
        return HerramientaResponse.desde(herramientaRepository.guardar(herramienta));
    }
}
