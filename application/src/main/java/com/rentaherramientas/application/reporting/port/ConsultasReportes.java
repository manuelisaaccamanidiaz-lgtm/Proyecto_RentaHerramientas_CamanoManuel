package com.rentaherramientas.application.reporting.port;

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
 * Puerto de salida de reporting: consultas agregadas de solo lectura sobre los
 * datos de todos los BC. reporting no tiene agregados propios; el adaptador las
 * resuelve con SQL directamente (SUM, COUNT, GROUP BY), sin pasar por los
 * repositorios de cada BC, porque cargar agregados para sumarlos sería muy lento.
 */
public interface ConsultasReportes {

    ResumenGeneralResponse resumenGeneral();

    /** desde/hasta incluidos, por la fecha del pago; meses sin ingresos no aparecen. */
    List<IngresoMensualResponse> ingresosPorMes(LocalDate desde, LocalDate hasta);

    List<HerramientaAlquiladaResponse> herramientasMasAlquiladas(int limite);

    List<ClienteFrecuenteResponse> clientesFrecuentes(int limite);

    InventarioResponse inventario();

    RentabilidadResponse rentabilidad(int limite);

    ResumenProveedorResponse resumenProveedor(Long proveedorId);
}
