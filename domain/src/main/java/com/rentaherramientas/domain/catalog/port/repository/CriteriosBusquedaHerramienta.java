package com.rentaherramientas.domain.catalog.port.repository;

import java.math.BigDecimal;

import com.rentaherramientas.domain.catalog.model.valueobject.EstadoHerramienta;
import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;
import com.rentaherramientas.domain.shared.model.SolicitudPagina;
import com.rentaherramientas.domain.shared.model.Validaciones;

/**
 * Filtros opcionales (null = sin filtrar) para buscar herramientas. El texto se
 * busca en nombre y descripción sin distinguir mayúsculas. Si excluirInactivas es
 * true y no se pide un estado concreto, se ocultan las herramientas dadas de baja.
 */
public record CriteriosBusquedaHerramienta(Long categoriaId,
                                           EstadoHerramienta estado,
                                           String texto,
                                           Long proveedorId, 
                                           String marca, 
                                           BigDecimal precioMin, 
                                           BigDecimal precioMax,
                                           boolean excluirInactivas,
                                           SolicitudPagina pagina) {

    public CriteriosBusquedaHerramienta {
        texto = texto == null || texto.isBlank() ? null : texto.strip();
        if(precioMin != null && precioMin.signum() < 0 || precioMax != null && precioMax.signum() < 0){
            throw new ValorInvalidoException("los precios no pueden ser negativos");
        }
        if(precioMin != null && precioMax != null && precioMin.compareTo(precioMax) > 0){
            throw new ValorInvalidoException("el precio minimo no puede ser mayor que el maximo");
        }
        Validaciones.requerido(pagina, "pagina");
    }
}
