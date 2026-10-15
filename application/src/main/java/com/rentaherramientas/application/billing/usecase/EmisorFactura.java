package com.rentaherramientas.application.billing.usecase;

import com.rentaherramientas.application.billing.port.GeneradorNumeroFactura;
import com.rentaherramientas.application.billing.port.ReservaFacturable;
import com.rentaherramientas.domain.billing.exception.FacturaInvalidaException;
import com.rentaherramientas.domain.billing.model.aggregate.Factura;
import com.rentaherramientas.domain.billing.model.aggregate.LineaFactura;
import com.rentaherramientas.domain.billing.model.aggregate.Pago;
import com.rentaherramientas.domain.billing.port.repository.FacturaRepository;
import com.rentaherramientas.domain.shared.model.valueobject.Dinero;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Emite la factura de un pago APROBADO: una línea "Alquiler de <herramienta> del
 * <inicio> al <fin>" con cantidad = días y valor unitario = tarifa diaria
 * pactada. Lo usan los dos casos de uso que aprueban pagos, dentro de su misma
 * unidad de trabajo, para que nunca exista un pago aprobado sin factura.
 */
final class EmisorFactura {

    static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final FacturaRepository facturaRepository;
    private final GeneradorNumeroFactura generadorNumeroFactura;
    private final Clock clock;

    EmisorFactura(FacturaRepository facturaRepository, GeneradorNumeroFactura generadorNumeroFactura, Clock clock) {
        this.facturaRepository = facturaRepository;
        this.generadorNumeroFactura = generadorNumeroFactura;
        this.clock = clock;
    }

    /** El total de la factura debe ser exactamente el monto cobrado; si no, no se emite. */
    Factura emitir(Pago pago, ReservaFacturable reserva) {
        if (!pago.estaAprobado()) {
            throw new FacturaInvalidaException("Solo se factura un pago APROBADO");
        }
        LocalDateTime ahora = LocalDateTime.now(clock);
        LineaFactura linea = LineaFactura.nueva(
                "Alquiler de " + reserva.nombreHerramienta()
                        + " del " + reserva.fechaInicio().format(FORMATO_FECHA)
                        + " al " + reserva.fechaFin().format(FORMATO_FECHA),
                Math.toIntExact(reserva.dias()),
                reserva.tarifaDiaria());
        Factura factura = Factura.emitir(generadorNumeroFactura.siguiente(ahora.getYear()), pago.getId(), ahora,
                List.of(linea));
        Dinero total = factura.total();
        if (!total.equals(pago.getMonto())) {
            throw new FacturaInvalidaException("El total de la factura (" + total.monto()
                    + ") no coincide con el monto del pago (" + pago.getMonto().monto() + ")");
        }
        return facturaRepository.guardar(factura);
    }
}
