-- =====================================================================
-- BC catalog: categorías, herramientas (una fila = una unidad física),
-- sus imágenes y la relación N:M herramienta-categoría.
-- =====================================================================

CREATE TABLE categorias (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre       VARCHAR(80) NOT NULL,
    descripcion  TEXT,
    CONSTRAINT uq_categorias_nombre          UNIQUE (nombre),
    CONSTRAINT ck_categorias_nombre_no_vacio CHECK (btrim(nombre) <> '')
);

CREATE TABLE herramientas (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    proveedor_id   BIGINT        NOT NULL,
    nombre         VARCHAR(120)  NOT NULL,
    descripcion    TEXT,
    tarifa_diaria  NUMERIC(12,2) NOT NULL,                 -- ┐ VO Dinero
    moneda         CHAR(3)       NOT NULL DEFAULT 'COP',   -- ┘ aplanado
    estado         VARCHAR(20)   NOT NULL DEFAULT 'DISPONIBLE',
    CONSTRAINT fk_herramientas_proveedor FOREIGN KEY (proveedor_id)
        REFERENCES proveedores (usuario_id),
    CONSTRAINT ck_herramientas_tarifa_positiva CHECK (tarifa_diaria > 0),
    CONSTRAINT ck_herramientas_moneda          CHECK (moneda ~ '^[A-Z]{3}$'),
    CONSTRAINT ck_herramientas_estado          CHECK (estado IN
        ('DISPONIBLE','EN_MANTENIMIENTO','DANADA','INACTIVA')),
    CONSTRAINT ck_herramientas_nombre_no_vacio CHECK (btrim(nombre) <> '')
);

CREATE INDEX ix_herramientas_proveedor_id ON herramientas (proveedor_id);

-- Entidad hija del agregado Herramienta: se borra con su herramienta.
CREATE TABLE herramienta_imagenes (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    herramienta_id  BIGINT       NOT NULL,
    url             VARCHAR(500) NOT NULL,
    orden           INTEGER      NOT NULL,
    CONSTRAINT fk_herramienta_imagenes_herramienta FOREIGN KEY (herramienta_id)
        REFERENCES herramientas (id) ON DELETE CASCADE,
    -- El índice de esta UNIQUE empieza por herramienta_id, así que también sirve
    -- como índice de la FK (no se crea uno aparte para no duplicarlo).
    -- DEFERRABLE: Hibernate hace los INSERT antes que los DELETE al sincronizar;
    -- si en una misma transacción se quita la imagen de orden 2 y se agrega otra
    -- con orden 2, la unicidad se comprueba al hacer COMMIT y no a mitad del flush.
    CONSTRAINT uq_herramienta_imagenes_orden UNIQUE (herramienta_id, orden)
        DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT ck_herramienta_imagenes_orden CHECK (orden >= 1)
);

-- N:M herramienta-categoría. Pertenece al agregado Herramienta (se borra con
-- ella); una categoría en uso no puede borrarse (FK sin cascade).
CREATE TABLE herramienta_categorias (
    herramienta_id  BIGINT NOT NULL,
    categoria_id    BIGINT NOT NULL,
    CONSTRAINT pk_herramienta_categorias PRIMARY KEY (herramienta_id, categoria_id),
    CONSTRAINT fk_herramienta_categorias_herramienta FOREIGN KEY (herramienta_id)
        REFERENCES herramientas (id) ON DELETE CASCADE,
    CONSTRAINT fk_herramienta_categorias_categoria FOREIGN KEY (categoria_id)
        REFERENCES categorias (id)
);

-- herramienta_id ya está cubierta por la PK compuesta (primera columna).
CREATE INDEX ix_herramienta_categorias_categoria_id ON herramienta_categorias (categoria_id);
