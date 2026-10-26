-- =====================================================================
-- BC identity: usuarios y datos propios de proveedores.
-- =====================================================================

CREATE TABLE usuarios (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre         VARCHAR(120) NOT NULL,
    email          VARCHAR(150) NOT NULL,   -- VO Email (normalizado a minúsculas)
    password_hash  VARCHAR(255) NOT NULL,
    rol            VARCHAR(20)  NOT NULL,
    activo         BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_usuarios_email           UNIQUE (email),
    CONSTRAINT ck_usuarios_rol             CHECK (rol IN ('ADMIN','PROVEEDOR','CLIENTE')),
    CONSTRAINT ck_usuarios_email_minusculas CHECK (email = lower(email)),
    CONSTRAINT ck_usuarios_nombre_no_vacio CHECK (btrim(nombre) <> '')
);

-- Relación 1:1 con usuarios: la PK es a la vez FK, así un usuario tiene como
-- máximo una fila de proveedor y clientes/admins no cargan columnas nulas.
CREATE TABLE proveedores (
    usuario_id    BIGINT       PRIMARY KEY,  -- PK = FK: ya está indexada
    razon_social  VARCHAR(150) NOT NULL,
    nit           VARCHAR(20)  NOT NULL,
    CONSTRAINT fk_proveedores_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT uq_proveedores_nit           UNIQUE (nit),
    CONSTRAINT ck_proveedores_nit_formato   CHECK (nit ~ '^[0-9]{6,10}(-[0-9])?$'),
    CONSTRAINT ck_proveedores_razon_social_no_vacia CHECK (btrim(razon_social) <> '')
);

-- Subtipo 1:1 de usuarios con los datos propios del cliente.
CREATE TABLE clientes (
    usuario_id  BIGINT       PRIMARY KEY,
    documento   VARCHAR(20)  NOT NULL,
    telefono    VARCHAR(20)  NOT NULL,
    direccion   VARCHAR(200) NOT NULL,
    CONSTRAINT fk_clientes_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT uq_clientes_documento UNIQUE (documento),
    CONSTRAINT ck_clientes_documento_formato CHECK (documento ~ '^[0-9]{5,15}$'),
    CONSTRAINT ck_clientes_telefono_formato  CHECK (telefono ~ '^\+?[0-9]{7,15}$'),
    CONSTRAINT ck_clientes_direccion_no_vacia CHECK (btrim(direccion) <> '')
);
