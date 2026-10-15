-- =====================================================================
-- BC billing: pagos y facturas con sus líneas.
-- Los totales de factura NO se guardan: se calculan como
-- SUM(cantidad * valor_unitario) sobre factura_lineas.
-- =====================================================================

CREATE TABLE pagos (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reserva_id  BIGINT        NOT NULL,
    monto       NUMERIC(12,2) NOT NULL,                 -- ┐ VO Dinero
    moneda      CHAR(3)       NOT NULL DEFAULT 'COP',   -- ┘ aplanado
    metodo      VARCHAR(20)   NOT NULL,
    estado      VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE',
    referencia  VARCHAR(60)   NOT NULL,
    fecha       TIMESTAMP     NOT NULL DEFAULT now(),
    CONSTRAINT fk_pagos_reserva FOREIGN KEY (reserva_id) REFERENCES reservas (id),
    CONSTRAINT uq_pagos_referencia UNIQUE (referencia),
    CONSTRAINT ck_pagos_monto_positivo CHECK (monto > 0),
    CONSTRAINT ck_pagos_moneda CHECK (moneda ~ '^[A-Z]{3}$'),
    CONSTRAINT ck_pagos_metodo CHECK (metodo IN ('TARJETA','PSE','EFECTIVO')),
    CONSTRAINT ck_pagos_estado CHECK (estado IN ('PENDIENTE','APROBADO','RECHAZADO'))
);

CREATE INDEX ix_pagos_reserva_id ON pagos (reserva_id);

CREATE TABLE facturas (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numero         VARCHAR(30) NOT NULL,
    pago_id        BIGINT      NOT NULL,
    moneda         CHAR(3)     NOT NULL DEFAULT 'COP',  -- moneda común de todas las líneas
    fecha_emision  TIMESTAMP   NOT NULL DEFAULT now(),
    CONSTRAINT uq_facturas_numero UNIQUE (numero),
    -- Un pago se factura una sola vez; esta UNIQUE también indexa pago_id.
    CONSTRAINT uq_facturas_pago   UNIQUE (pago_id),
    CONSTRAINT fk_facturas_pago FOREIGN KEY (pago_id) REFERENCES pagos (id),
    CONSTRAINT ck_facturas_moneda CHECK (moneda ~ '^[A-Z]{3}$'),
    CONSTRAINT ck_facturas_numero_formato CHECK (numero ~ '^[A-Z0-9-]{1,30}$')
);

-- Entidad hija del agregado Factura. Sin columna subtotal (derivable).
-- La moneda vive en facturas: depende de la factura, no de cada línea.
CREATE TABLE factura_lineas (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    factura_id      BIGINT        NOT NULL,
    descripcion     VARCHAR(255)  NOT NULL,
    cantidad        INTEGER       NOT NULL,
    valor_unitario  NUMERIC(12,2) NOT NULL,
    CONSTRAINT fk_factura_lineas_factura FOREIGN KEY (factura_id)
        REFERENCES facturas (id) ON DELETE CASCADE,
    CONSTRAINT ck_factura_lineas_cantidad_positiva CHECK (cantidad > 0),
    CONSTRAINT ck_factura_lineas_valor_no_negativo CHECK (valor_unitario >= 0)
);

CREATE INDEX ix_factura_lineas_factura_id ON factura_lineas (factura_id);
