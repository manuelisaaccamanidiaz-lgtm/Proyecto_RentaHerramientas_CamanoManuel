package com.rentaherramientas.infrastructure.billing.adapters.out.persistence;

import com.rentaherramientas.application.billing.port.GeneradorNumeroFactura;
import com.rentaherramientas.domain.billing.model.valueobject.NumeroFactura;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/**
 * Numera las facturas con la SEQUENCE seq_facturas (V6). nextval es atómico y
 * no se deshace con un rollback, así que dos emisiones simultáneas nunca
 * reciben el mismo número (a cambio, un rollback deja un hueco en la numeración).
 */
@Component
public class GeneradorNumeroFacturaSecuencia implements GeneradorNumeroFactura {

    private final JdbcClient jdbcClient;

    public GeneradorNumeroFacturaSecuencia(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public NumeroFactura siguiente(int anio) {
        long consecutivo = jdbcClient.sql("select nextval('seq_facturas')").query(Long.class).single();
        return new NumeroFactura("FAC-%d-%06d".formatted(anio, consecutivo));
    }
}
