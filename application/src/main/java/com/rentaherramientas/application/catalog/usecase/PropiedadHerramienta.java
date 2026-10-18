package com.rentaherramientas.application.catalog.usecase;

import com.rentaherramientas.application.catalog.exception.HerramientaNoEncontradaException;
import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.application.shared.exception.AccesoDenegadoException;
import com.rentaherramientas.domain.catalog.model.aggregate.Herramienta;
import com.rentaherramientas.domain.catalog.port.repository.HerramientaRepository;

/**
 * Regla de propiedad compartida por los casos de uso que modifican herramientas:
 * un PROVEEDOR solo modifica las suyas y un ADMIN cualquiera. Primero se comprueba
 * que exista (404) y luego la propiedad (403).
 */
final class PropiedadHerramienta {

    /** Constructor privado: clase de utilidades estáticas. */
    private PropiedadHerramienta() {
    }

    /** Carga la herramienta y comprueba que el solicitante sea su dueño o un ADMIN. */
    static Herramienta cargarParaModificar(HerramientaRepository repository, Long herramientaId,
                                           Solicitante solicitante) {
        Herramienta herramienta = repository.buscarPorId(herramientaId)
                .orElseThrow(() -> new HerramientaNoEncontradaException(herramientaId));
        if (!solicitante.esAdmin() && !herramienta.perteneceA(solicitante.id())) {
            throw new AccesoDenegadoException("No puedes modificar una herramienta de otro proveedor");
        }
        return herramienta;
    }
}
