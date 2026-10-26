package com.rentaherramientas.application.reporting.port;

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
 * Puerto de salida de reporting: consultas agregadas de solo lectura sobre los
 * datos de todos los BC. reporting no tiene agregados propios; el adaptador las
 * resuelve con SQL directamente (SUM, COUNT, GROUP BY), sin pasar por los
 * repositorios de cada BC, porque cargar agregados para sumarlos sería muy lento.
 */
public interface ConsultasReportes {

    /**
     * Totales generales: ingresos por moneda, pagos aprobados, facturas emitidas, reservas por estado y usuarios por rol.
     */
    ResumenGeneralResponse resumenGeneral();

    /** desde/hasta incluidos, por la fecha del pago; meses sin ingresos no aparecen. */
    List<IngresoMensualResponse> ingresosPorMes(LocalDate desde, LocalDate hasta);

    /** Las herramientas con más reservas, hasta `limite` resultados. */
    List<HerramientaAlquiladaResponse> herramientasMasAlquiladas(int limite);

    /** Los clientes con más alquileres y lo que han pagado, hasta `limite` resultados. */
    List<ClienteFrecuenteResponse> clientesFrecuentes(int limite);

    /** Total de herramientas y su conteo por estado y por categoría. */
    InventarioResponse inventario();

    /** Herramientas y proveedores que más ingresos generan, hasta `limite` de cada uno. */
    RentabilidadResponse rentabilidad(int limite);

    /** Resumen de un proveedor: ingresos, pagos aprobados, reservas y herramientas por estado. */
    ResumenProveedorResponse resumenProveedor(Long proveedorId);

    List<HerramientaDaniosResponse> herramientasConMasDanios(int limite);
}
