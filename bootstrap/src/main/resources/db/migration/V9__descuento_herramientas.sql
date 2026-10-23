-- Descuento porcentual opcional sobre la tarifa diaria (NULL = sin descuento).
-- Solo afecta a reservas nuevas: reservas.precio_total es una foto del momento de reservar.
ALTER TABLE herramientas ADD COLUMN descuento_porcentaje INTEGER
    CONSTRAINT ck_herramientas_descuento CHECK (descuento_porcentaje BETWEEN 1 AND 90);
