package com.rentaherramientas.application.rental.usecase;

import com.rentaherramientas.application.rental.command.ReportarDanioCommand;
import com.rentaherramientas.application.rental.dto.ReservaResponse;
import com.rentaherramientas.application.rental.port.CatalogoHerramientas;
import com.rentaherramientas.application.rental.port.ConsultaPagos;
import com.rentaherramientas.domain.rental.model.aggregate.Reserva;
import com.rentaherramientas.domain.rental.port.repository.ReservaRepository;
import com.rentaherramientas.domain.shared.model.valueobject.Dinero;

/**
 * Agrega el reporte a la reserva (el agregado exige que esté DEVUELTA) y luego
 * pide al catálogo que marque la herramienta como DANADA, para que no se pueda
 * volver a reservar hasta que el proveedor la repare.
 */
public class ReportarDanioService implements ReportarDanioUseCase {

    private final ReservaRepository reservaRepository;
    private final CatalogoHerramientas catalogoHerramientas;
    private final ConsultaPagos consultaPagos;

    /** Recibe sus dependencias por constructor; el bean lo crea RentalBeansConfig. */
    public ReportarDanioService(ReservaRepository reservaRepository, CatalogoHerramientas catalogoHerramientas,
                                ConsultaPagos consultaPagos) {
        this.reservaRepository = reservaRepository;
        this.catalogoHerramientas = catalogoHerramientas;
        this.consultaPagos = consultaPagos;
    }

    /**
     * Añade un reporte de daño a la reserva y marca la herramienta como DANADA en el catálogo.
     * El costo estimado, si se envía, usa la misma moneda que el precio de la reserva.
     */
    @Override
    public ReservaResponse reportar(ReportarDanioCommand command) {
        Reserva reserva = PropiedadReserva.cargarParaGestionar(reservaRepository, catalogoHerramientas,
                command.reservaId(), command.solicitante());
        Dinero costo = command.costoEstimado() == null
                ? null
                : Dinero.de(command.costoEstimado(), reserva.getPrecioTotal().moneda());
        reserva.reportarDanio(command.descripcion(), costo);
        Reserva guardada = reservaRepository.guardar(reserva);
        catalogoHerramientas.marcarDanada(guardada.getHerramientaId());
        return ReservaResponse.desde(guardada, consultaPagos.tienePagoAprobado(guardada.getId()));
    }
}
