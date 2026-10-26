-- =====================================================================
-- BC billing: numeración de facturas, unicidad del pago vigente e índices
-- para los reportes (BC reporting).
-- =====================================================================

-- Consecutivo de facturas: FAC-<año>-<6 dígitos>. Una SEQUENCE es atómica ante
-- emisiones simultáneas (dos facturas nunca reciben el mismo número). nextval no
-- se deshace con un rollback: una emisión fallida deja un hueco, nunca un duplicado.
CREATE SEQUENCE seq_facturas AS BIGINT START WITH 1 INCREMENT BY 1 NO CYCLE;

-- Una reserva tiene como máximo UN pago vigente (PENDIENTE o APROBADO); los
-- RECHAZADOS no cuentan, así el cliente puede reintentar. El caso de uso lo
-- comprueba antes, y este índice cierra la carrera entre dos pagos simultáneos
-- (el adaptador traduce la violación a PagoDuplicadoException → 409).
CREATE UNIQUE INDEX uq_pagos_reserva_vigente ON pagos (reserva_id)
    WHERE estado IN ('PENDIENTE', 'APROBADO');

-- Reportes de ingresos: filtran pagos APROBADOS por rango de fecha y agrupan por mes.
CREATE INDEX ix_pagos_estado_fecha ON pagos (estado, fecha);

-- Reportes y vencimiento de reservas filtran o agrupan por estado.
CREATE INDEX ix_reservas_estado ON reservas (estado);

-- Inventario y resumen del proveedor agrupan herramientas por estado.
CREATE INDEX ix_herramientas_estado ON herramientas (estado);
