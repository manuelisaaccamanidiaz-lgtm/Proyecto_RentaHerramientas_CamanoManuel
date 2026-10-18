package com.rentaherramientas.infrastructure.billing.adapters.out.persistence;

import com.rentaherramientas.application.billing.port.GeneradorNumeroFactura;
import com.rentaherramientas.domain.billing.model.valueobject.NumeroFactura;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/**
 * Numera las facturas con la SEQUENCE seq_facturas (V6). nextval es atómico y
 * no se deshace con un rollback, así que dos emisiones simultáneas nunca
 * reciben el mismo número (a cambio, un rollback deja un hueco en la numeración).
 *
 * TÉCNICA AVANZADA (Spring 6.1): `JdbcClient`
 *   Qué hace: API fluida para SQL nativo: `jdbcClient.sql("...").param(...).query(Tipo.class).single()`. Es
 *     más corta que `JdbcTemplate` y la configura Spring Boot automáticamente.
 */
@Component
public class GeneradorNumeroFacturaSecuencia implements GeneradorNumeroFactura {

    private final JdbcClient jdbcClient;

    /** Recibe el JdbcClient que configura Spring Boot. */
    public GeneradorNumeroFacturaSecuencia(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    /**
     * Pide el siguiente valor a la secuencia y lo formatea como FAC-2026-000001.
     *
     * Nota: `"FAC-%d-%06d".formatted(...)` rellena con ceros a la izquierda hasta 6 dígitos.
     *
     * TÉCNICA AVANZADA (PostgreSQL): SEQUENCE
     *   Qué hace: `nextval('seq_facturas')` devuelve el siguiente número de forma atómica aunque haya
     *     transacciones simultáneas, y nunca se deshace con un rollback (puede dejar huecos, nunca
     *     duplicados).
     *   Cómo usarlo: `CREATE SEQUENCE nombre;` y `SELECT nextval('nombre');`
     */
    @Override
    public NumeroFactura siguiente(int anio) {
        long consecutivo = jdbcClient.sql("select nextval('seq_facturas')").query(Long.class).single();
        return new NumeroFactura("FAC-%d-%06d".formatted(anio, consecutivo));
    }
}
