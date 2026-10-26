-- =====================================================================
-- BC notification: avisos dentro de la app para cada usuario.
-- =====================================================================

CREATE TABLE notificaciones (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id  BIGINT       NOT NULL,
    mensaje     VARCHAR(300) NOT NULL,
    leida       BOOLEAN      NOT NULL DEFAULT FALSE,
    creada_en   TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT fk_notificaciones_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT ck_notificaciones_mensaje_no_vacio CHECK (btrim(mensaje) <> '')
);

-- Sirve a las dos consultas de la campana: el conteo (usuario_id, leida = false)
-- y el listado del usuario (prefijo usuario_id).
CREATE INDEX ix_notificaciones_usuario_leida ON notificaciones (usuario_id, leida);
