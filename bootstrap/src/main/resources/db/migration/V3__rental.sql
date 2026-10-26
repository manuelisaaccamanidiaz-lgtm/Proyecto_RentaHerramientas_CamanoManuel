CREATE TABLE reservas (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    herramienta_id    BIGINT        NOT NULL,
    cliente_id        BIGINT        NOT NULL,
    fecha_inicio      DATE          NOT NULL,
    fecha_fin         DATE          NOT NULL,
    estado            VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE',
    precio_total      NUMERIC(12,2) NOT NULL,
    moneda            CHAR(3)       NOT NULL DEFAULT 'COP',
    fecha_entrega     TIMESTAMP,
    fecha_devolucion  TIMESTAMP,
    creado_en         TIMESTAMP     NOT NULL DEFAULT now(),
    CONSTRAINT fk_reservas_herramienta FOREIGN KEY (herramienta_id) REFERENCES herramientas (id),
    CONSTRAINT fk_reservas_cliente     FOREIGN KEY (cliente_id)     REFERENCES clientes (usuario_id),
    CONSTRAINT ck_reservas_fechas      CHECK (fecha_fin >= fecha_inicio),
    CONSTRAINT ck_reservas_estado      CHECK (estado IN
        ('PENDIENTE','ACEPTADA','RECHAZADA','ENTREGADA','DEVUELTA','VENCIDA')),
    CONSTRAINT ck_reservas_precio_positivo CHECK (precio_total > 0),
    CONSTRAINT ck_reservas_moneda      CHECK (moneda ~ '^[A-Z]{3}$'),
    CONSTRAINT ck_reservas_devolucion_tras_entrega CHECK (
        fecha_devolucion IS NULL
        OR (fecha_entrega IS NOT NULL AND fecha_devolucion >= fecha_entrega)),
    CONSTRAINT ck_reservas_entrega_segun_estado CHECK (
        (estado IN ('ENTREGADA','DEVUELTA')) = (fecha_entrega IS NOT NULL)),
    CONSTRAINT ck_reservas_devolucion_segun_estado CHECK (
        (estado = 'DEVUELTA') = (fecha_devolucion IS NOT NULL))
);

CREATE INDEX ix_reservas_herramienta_fechas ON reservas (herramienta_id, fecha_inicio, fecha_fin);
CREATE INDEX ix_reservas_cliente_id ON reservas (cliente_id);

CREATE TABLE reportes_danio (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reserva_id      BIGINT        NOT NULL,
    descripcion     TEXT          NOT NULL,
    costo_estimado  NUMERIC(12,2),
    reportado_en    TIMESTAMP     NOT NULL DEFAULT now(),
    CONSTRAINT fk_reportes_danio_reserva FOREIGN KEY (reserva_id)
        REFERENCES reservas (id) ON DELETE CASCADE,
    CONSTRAINT ck_reportes_danio_costo_no_negativo CHECK (costo_estimado IS NULL OR costo_estimado >= 0),
    CONSTRAINT ck_reportes_danio_descripcion_no_vacia CHECK (btrim(descripcion) <> '')
);

CREATE INDEX ix_reportes_danio_reserva_id ON reportes_danio (reserva_id);
