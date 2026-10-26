package com.rentaherramientas.application.rental.usecase;

import com.rentaherramientas.application.rental.command.ReportarDanioCommand;
import com.rentaherramientas.application.rental.dto.ReservaResponse;

/**
 * El proveedor (o un ADMIN) reporta un daño al inspeccionar una herramienta devuelta.
 */
public interface ReportarDanioUseCase {

    /** Reporta un daño en la reserva indicada en el comando. */
    ReservaResponse reportar(ReportarDanioCommand command);
}
