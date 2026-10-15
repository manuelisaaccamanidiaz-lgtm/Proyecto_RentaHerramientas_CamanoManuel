package com.rentaherramientas.infrastructure.reporting.adapters.out.persistence;

import com.rentaherramientas.application.reporting.dto.ClienteFrecuenteResponse;
import com.rentaherramientas.application.reporting.dto.HerramientaAlquiladaResponse;
import com.rentaherramientas.application.reporting.dto.IngresoMensualResponse;
import com.rentaherramientas.application.reporting.dto.InventarioResponse;
import com.rentaherramientas.application.reporting.dto.RentabilidadResponse;
import com.rentaherramientas.application.reporting.dto.ResumenGeneralResponse;
import com.rentaherramientas.application.reporting.dto.ResumenProveedorResponse;
import com.rentaherramientas.application.reporting.port.ConsultasReportes;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Adaptador de salida de reporting: SQL nativo de solo lectura con JdbcClient.
 * Agrega en la base de datos (SUM, COUNT, GROUP BY, date_trunc) en lugar de
 * cargar agregados en memoria. Convenciones de todos los reportes:
 * - "ingresos" = pagos APROBADOS, agrupados por moneda (nunca se suman monedas distintas);
 * - "alquiler" = reserva que llegó a entregarse (ENTREGADA o DEVUELTA).
 * Los parámetros siempre van enlazados (:nombre), nunca concatenados en el SQL.
 */
@Component
@Transactional(readOnly = true)
public class ConsultasReportesJdbc implements ConsultasReportes {

    private static final String ESTADOS_ALQUILADA = "('ENTREGADA', 'DEVUELTA')";

    private final JdbcClient jdbc;

    public ConsultasReportesJdbc(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public ResumenGeneralResponse resumenGeneral() {
        Map<String, BigDecimal> ingresos = montosPorClave("""
                select moneda as clave, sum(monto) as total
                from pagos where estado = 'APROBADO'
                group by moneda order by moneda
                """, Map.of());
        long pagosAprobados = contar("select count(*) from pagos where estado = 'APROBADO'", Map.of());
        long facturas = contar("select count(*) from facturas", Map.of());
        return new ResumenGeneralResponse(ingresos, pagosAprobados, facturas,
                conteoPorClave("select estado as clave, count(*) as total from reservas group by estado order by estado",
                        Map.of()),
                conteoPorClave("select rol as clave, count(*) as total from usuarios group by rol order by rol",
                        Map.of()),
                conteoPorClave("select estado as clave, count(*) as total from herramientas group by estado order by estado",
                        Map.of()));
    }

    /** date_trunc agrupa por mes calendario; el filtro usa [desde 00:00, hasta+1 00:00) para incluir todo "hasta". */
    @Override
    public List<IngresoMensualResponse> ingresosPorMes(LocalDate desde, LocalDate hasta) {
        return jdbc.sql("""
                        select to_char(date_trunc('month', p.fecha), 'YYYY-MM') as mes,
                               p.moneda, sum(p.monto) as total, count(*) as pagos
                        from pagos p
                        where p.estado = 'APROBADO'
                          and p.fecha >= :inicio and p.fecha < :finExclusivo
                        group by date_trunc('month', p.fecha), p.moneda
                        order by date_trunc('month', p.fecha), p.moneda
                        """)
                .param("inicio", desde.atStartOfDay())
                .param("finExclusivo", hasta.plusDays(1).atStartOfDay())
                .query((rs, fila) -> new IngresoMensualResponse(rs.getString("mes"), rs.getString("moneda"),
                        rs.getBigDecimal("total"), rs.getLong("pagos")))
                .list();
    }

    @Override
    public List<HerramientaAlquiladaResponse> herramientasMasAlquiladas(int limite) {
        return jdbc.sql("""
                        select h.id, h.nombre, pr.razon_social as proveedor,
                               count(r.id) as alquileres,
                               sum(r.fecha_fin - r.fecha_inicio + 1) as dias
                        from reservas r
                        join herramientas h on h.id = r.herramienta_id
                        join proveedores pr on pr.usuario_id = h.proveedor_id
                        where r.estado in %s
                        group by h.id, h.nombre, pr.razon_social
                        order by alquileres desc, dias desc, h.id
                        limit :limite
                        """.formatted(ESTADOS_ALQUILADA))
                .param("limite", limite)
                .query((rs, fila) -> new HerramientaAlquiladaResponse(rs.getLong("id"), rs.getString("nombre"),
                        rs.getString("proveedor"), rs.getLong("alquileres"), rs.getLong("dias")))
                .list();
    }

    /**
     * El LEFT JOIN con pagos solo toma el APROBADO, y el índice único parcial
     * garantiza que hay como mucho uno por reserva: el JOIN no duplica reservas.
     */
    @Override
    public List<ClienteFrecuenteResponse> clientesFrecuentes(int limite) {
        return jdbc.sql("""
                        select u.id, u.nombre, u.email, r.moneda,
                               count(r.id) as alquileres,
                               coalesce(sum(p.monto), 0) as total_pagado
                        from reservas r
                        join usuarios u on u.id = r.cliente_id
                        left join pagos p on p.reserva_id = r.id and p.estado = 'APROBADO'
                        where r.estado in %s
                        group by u.id, u.nombre, u.email, r.moneda
                        order by alquileres desc, total_pagado desc, u.id
                        limit :limite
                        """.formatted(ESTADOS_ALQUILADA))
                .param("limite", limite)
                .query((rs, fila) -> new ClienteFrecuenteResponse(rs.getLong("id"), rs.getString("nombre"),
                        rs.getString("email"), rs.getLong("alquileres"), rs.getString("moneda"),
                        rs.getBigDecimal("total_pagado")))
                .list();
    }

    /** Categorías sin herramientas aparecen con 0 (LEFT JOIN). */
    @Override
    public InventarioResponse inventario() {
        List<InventarioResponse.ConteoCategoria> porCategoria = jdbc.sql("""
                        select c.id, c.nombre, count(hc.herramienta_id) as herramientas
                        from categorias c
                        left join herramienta_categorias hc on hc.categoria_id = c.id
                        group by c.id, c.nombre
                        order by herramientas desc, c.nombre
                        """)
                .query((rs, fila) -> new InventarioResponse.ConteoCategoria(rs.getLong("id"), rs.getString("nombre"),
                        rs.getLong("herramientas")))
                .list();
        return new InventarioResponse(
                contar("select count(*) from herramientas", Map.of()),
                conteoPorClave("select estado as clave, count(*) as total from herramientas group by estado order by estado",
                        Map.of()),
                porCategoria);
    }

    @Override
    public RentabilidadResponse rentabilidad(int limite) {
        List<RentabilidadResponse.IngresoHerramienta> porHerramienta = jdbc.sql("""
                        select h.id, h.nombre, pr.razon_social as proveedor, p.moneda,
                               sum(p.monto) as ingresos, count(p.id) as pagos
                        from pagos p
                        join reservas r on r.id = p.reserva_id
                        join herramientas h on h.id = r.herramienta_id
                        join proveedores pr on pr.usuario_id = h.proveedor_id
                        where p.estado = 'APROBADO'
                        group by h.id, h.nombre, pr.razon_social, p.moneda
                        order by ingresos desc, h.id
                        limit :limite
                        """)
                .param("limite", limite)
                .query((rs, fila) -> new RentabilidadResponse.IngresoHerramienta(rs.getLong("id"),
                        rs.getString("nombre"), rs.getString("proveedor"), rs.getString("moneda"),
                        rs.getBigDecimal("ingresos"), rs.getLong("pagos")))
                .list();
        List<RentabilidadResponse.IngresoProveedor> porProveedor = jdbc.sql("""
                        select pr.usuario_id as id, pr.razon_social, p.moneda,
                               sum(p.monto) as ingresos, count(p.id) as pagos
                        from pagos p
                        join reservas r on r.id = p.reserva_id
                        join herramientas h on h.id = r.herramienta_id
                        join proveedores pr on pr.usuario_id = h.proveedor_id
                        where p.estado = 'APROBADO'
                        group by pr.usuario_id, pr.razon_social, p.moneda
                        order by ingresos desc, pr.usuario_id
                        limit :limite
                        """)
                .param("limite", limite)
                .query((rs, fila) -> new RentabilidadResponse.IngresoProveedor(rs.getLong("id"),
                        rs.getString("razon_social"), rs.getString("moneda"), rs.getBigDecimal("ingresos"),
                        rs.getLong("pagos")))
                .list();
        return new RentabilidadResponse(porHerramienta, porProveedor);
    }

    @Override
    public ResumenProveedorResponse resumenProveedor(Long proveedorId) {
        Map<String, Object> proveedor = Map.of("proveedorId", proveedorId);
        Map<String, BigDecimal> ingresos = montosPorClave("""
                select p.moneda as clave, sum(p.monto) as total
                from pagos p
                join reservas r on r.id = p.reserva_id
                join herramientas h on h.id = r.herramienta_id
                where p.estado = 'APROBADO' and h.proveedor_id = :proveedorId
                group by p.moneda order by p.moneda
                """, proveedor);
        long pagosAprobados = contar("""
                select count(*)
                from pagos p
                join reservas r on r.id = p.reserva_id
                join herramientas h on h.id = r.herramienta_id
                where p.estado = 'APROBADO' and h.proveedor_id = :proveedorId
                """, proveedor);
        Map<String, Long> reservasPorEstado = conteoPorClave("""
                select r.estado as clave, count(*) as total
                from reservas r
                join herramientas h on h.id = r.herramienta_id
                where h.proveedor_id = :proveedorId
                group by r.estado order by r.estado
                """, proveedor);
        Map<String, Long> herramientasPorEstado = conteoPorClave("""
                select estado as clave, count(*) as total
                from herramientas where proveedor_id = :proveedorId
                group by estado order by estado
                """, proveedor);
        return new ResumenProveedorResponse(proveedorId, ingresos, pagosAprobados, reservasPorEstado,
                herramientasPorEstado);
    }

    // ------------------------------------------------------------ utilidades

    private long contar(String sql, Map<String, ?> parametros) {
        return jdbc.sql(sql).params(parametros).query(Long.class).single();
    }

    /** Consulta con columnas (clave, total) → mapa ordenado como la consulta. */
    private Map<String, Long> conteoPorClave(String sql, Map<String, ?> parametros) {
        Map<String, Long> resultado = new LinkedHashMap<>();
        jdbc.sql(sql).params(parametros).query((ResultSet rs) -> {
            resultado.put(rs.getString("clave"), rs.getLong("total"));
        });
        return resultado;
    }

    private Map<String, BigDecimal> montosPorClave(String sql, Map<String, ?> parametros) {
        Map<String, BigDecimal> resultado = new LinkedHashMap<>();
        jdbc.sql(sql).params(parametros).query((ResultSet rs) -> {
            resultado.put(rs.getString("clave"), montoDe(rs));
        });
        return resultado;
    }

    private static BigDecimal montoDe(ResultSet rs) throws SQLException {
        BigDecimal total = rs.getBigDecimal("total");
        return total == null ? BigDecimal.ZERO : total;
    }
}
