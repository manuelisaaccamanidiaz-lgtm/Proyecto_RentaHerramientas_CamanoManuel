package com.rentaherramientas.application.rental.port;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida de rental hacia el catálogo (capa anticorrupción): rental
 * solo ve lo que necesita de una herramienta, con su propio modelo, y nunca
 * importa clases de catalog. El adaptador vive en infraestructura.
 */
public interface CatalogoHerramientas {

    /** Devuelve los datos de la herramienta que necesita rental (dueño, tarifa, disponibilidad). */
    Optional<HerramientaReservable> buscar(Long herramientaId);

    /** Ids de todas las herramientas del proveedor (incluidas las inactivas, que pueden tener historial). */
    List<Long> idsDelProveedor(Long proveedorId);

    /** Marca la herramienta como DANADA tras un reporte de daño; si ya no puede pasar a ese estado, no hace nada. */
    void marcarDanada(Long herramientaId);
}
