package com.rentaherramientas.application.reporting.usecase;

import com.rentaherramientas.application.reporting.dto.ClienteFrecuenteResponse;
import com.rentaherramientas.application.reporting.dto.HerramientaAlquiladaResponse;
import com.rentaherramientas.application.reporting.dto.HerramientaDaniosResponse;
import com.rentaherramientas.application.reporting.dto.IngresoMensualResponse;
import com.rentaherramientas.application.reporting.dto.InventarioResponse;
import com.rentaherramientas.application.reporting.dto.RentabilidadResponse;
import com.rentaherramientas.application.reporting.dto.ResumenGeneralResponse;
import com.rentaherramientas.application.reporting.dto.ResumenProveedorResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * Reportes de la plataforma (ADMIN) y el resumen de cada proveedor sobre sus herramientas.
 */
public interface ConsultarReportesUseCase {

    /** Resumen general del sistema. */
    ResumenGeneralResponse resumenGeneral();

    /** desde null = primer día del mes, 11 meses atrás; hasta null = hoy. */
    List<IngresoMensualResponse> ingresosPorMes(LocalDate desde, LocalDate hasta);

    /** Herramientas más alquiladas. */
    List<HerramientaAlquiladaResponse> herramientasMasAlquiladas(int limite);

    /** Clientes con más alquileres. */
    List<ClienteFrecuenteResponse> clientesFrecuentes(int limite);

    /** Inventario de herramientas por estado y por categoría. */
    InventarioResponse inventario();

    /** Herramientas y proveedores más rentables. */
    RentabilidadResponse rentabilidad(int limite);

    /** Resumen de un proveedor. */
    ResumenProveedorResponse resumenProveedor(Long proveedorId);

    List<HerramientaDaniosResponse> herramientasConMasDanios(int limite);

}
