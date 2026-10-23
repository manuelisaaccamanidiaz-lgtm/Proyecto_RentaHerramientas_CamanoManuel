-- =====================================================================
-- BC catalog: herramientas que un cliente marcó como favoritas.
-- =====================================================================
--
-- Por qué está en 4FN (y por tanto en 3FN/FNBC):
--   - La PK compuesta (cliente_id, herramienta_id) es la única clave candidata y
--     el único atributo no clave, creado_en, depende de la clave completa (el
--     momento en que ESE cliente marcó ESA herramienta): no hay dependencias
--     parciales (2FN) ni transitivas (3FN), y todo determinante es clave (FNBC).
--   - No hay dependencias multivaluadas no triviales: la tabla registra un solo
--     hecho, "el cliente C marcó la herramienta H". Nada de la herramienta
--     (nombre, tarifa, estado) ni del cliente se copia aquí; esos datos viven en
--     herramientas y clientes. Mezclar en la misma fila otro hecho independiente
--     del cliente (p. ej. sus categorías preferidas) sí rompería la 4FN.
--
-- Ambas FK van con ON DELETE CASCADE: un favorito es una preferencia sin valor
-- histórico, así que si desaparece el cliente (p. ej. cambia de rol) o la
-- herramienta, la marca no tiene sentido y no debe impedir el borrado.
CREATE TABLE favoritos (
    cliente_id      BIGINT    NOT NULL,
    herramienta_id  BIGINT    NOT NULL,
    creado_en       TIMESTAMP NOT NULL DEFAULT now(),
    -- La PK impide duplicados: marcar dos veces la misma herramienta no crea otra fila.
    CONSTRAINT pk_favoritos PRIMARY KEY (cliente_id, herramienta_id),
    CONSTRAINT fk_favoritos_cliente FOREIGN KEY (cliente_id)
        REFERENCES clientes (usuario_id) ON DELETE CASCADE,
    CONSTRAINT fk_favoritos_herramienta FOREIGN KEY (herramienta_id)
        REFERENCES herramientas (id) ON DELETE CASCADE
);

-- cliente_id ya está cubierta por la PK compuesta (primera columna). Este índice
-- sirve a la FK de herramienta_id (borrados en cascada) y a contar favoritos por herramienta.
CREATE INDEX ix_favoritos_herramienta_id ON favoritos (herramienta_id);
