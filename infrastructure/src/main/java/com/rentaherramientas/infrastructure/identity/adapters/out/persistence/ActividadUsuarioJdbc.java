package com.rentaherramientas.infrastructure.identity.adapters.out.persistence;

import com.rentaherramientas.application.identity.port.ActividadUsuario;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/**
 * Implementa el puerto anticorrupción con SQL directo sobre las tablas de otros
 * BC (reservas, herramientas): así identity no importa entidades de rental ni
 * de catalog, solo conoce el nombre de dos columnas.
 *
 * Nota: `select exists(...)` se detiene en la primera fila encontrada y siempre
 *   devuelve una fila (true/false), por eso `single()` es seguro.
 */
@Component
public class ActividadUsuarioJdbc implements ActividadUsuario {

    private final JdbcClient jdbcClient;

    /** Recibe el JdbcClient que configura Spring Boot. */
    public ActividadUsuarioJdbc(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    /** Usa el índice ix_reservas_cliente_id (V3). */
    @Override
    public boolean tieneReservasComoCliente(Long usuarioId) {
        return existe("select exists(select 1 from reservas where cliente_id = :id)", usuarioId);
    }

    /** Usa el índice ix_herramientas_proveedor_id (V2). */
    @Override
    public boolean tieneHerramientasComoProveedor(Long usuarioId) {
        return existe("select exists(select 1 from herramientas where proveedor_id = :id)", usuarioId);
    }

    private boolean existe(String sql, Long usuarioId) {
        return jdbcClient.sql(sql).param("id", usuarioId).query(Boolean.class).single();
    }
}
