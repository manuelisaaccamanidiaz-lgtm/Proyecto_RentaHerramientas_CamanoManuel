package com.rentaherramientas.application.reporting.usecase;

import com.rentaherramientas.application.reporting.dto.ClienteFrecuenteResponse;
import com.rentaherramientas.application.reporting.dto.HerramientaAlquiladaResponse;
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

    ResumenGeneralResponse resumenGeneral();

    /** desde null = primer día del mes, 11 meses atrás; hasta null = hoy. */
    List<IngresoMensualResponse> ingresosPorMes(LocalDate desde, LocalDate hasta);

    List<HerramientaAlquiladaResponse> herramientasMasAlquiladas(int limite);

    List<ClienteFrecuenteResponse> clientesFrecuentes(int limite);

    InventarioResponse inventario();

    RentabilidadResponse rentabilidad(int limite);

    ResumenProveedorResponse resumenProveedor(Long proveedorId);
}
