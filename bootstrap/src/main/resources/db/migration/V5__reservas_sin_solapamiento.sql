-- =====================================================================
-- BC rental: una herramienta no puede tener dos reservas activas con
-- fechas que se crucen. El caso de uso ya lo comprueba antes de insertar,
-- pero dos clientes que reservan a la vez pasan ambos esa comprobación;
-- esta restricción de exclusión hace que la BD rechace al segundo
-- (SQLState 23P01, que el adaptador traduce a ReservaSolapadaException).
-- =====================================================================

-- Permite usar el operador "=" sobre BIGINT dentro de un índice GiST.
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- '[]' = ambos extremos incluidos, igual que el value object RangoFechas.
-- Solo los estados que bloquean disponibilidad (EstadoReserva#bloqueaDisponibilidad).
ALTER TABLE reservas
    ADD CONSTRAINT ex_reservas_sin_solapamiento
    EXCLUDE USING gist (
        herramienta_id WITH =,
        daterange(fecha_inicio, fecha_fin, '[]') WITH &&
    )
    WHERE (estado IN ('PENDIENTE', 'ACEPTADA', 'ENTREGADA'));
