package com.rentaherramientas.infrastructure.billing.config;

import com.rentaherramientas.application.billing.port.ConsultaReservas;
import com.rentaherramientas.application.billing.port.ConsultaTerceros;
import com.rentaherramientas.application.billing.port.GeneradorNumeroFactura;
import com.rentaherramientas.application.billing.port.GeneradorPdfFactura;
import com.rentaherramientas.application.billing.usecase.ConfirmarPagoService;
import com.rentaherramientas.application.billing.usecase.ConfirmarPagoUseCase;
import com.rentaherramientas.application.billing.usecase.ConsultarFacturasService;
import com.rentaherramientas.application.billing.usecase.ConsultarFacturasUseCase;
import com.rentaherramientas.application.billing.usecase.ConsultarPagosService;
import com.rentaherramientas.application.billing.usecase.ConsultarPagosUseCase;
import com.rentaherramientas.application.billing.usecase.PagarReservaService;
import com.rentaherramientas.application.billing.usecase.PagarReservaUseCase;
import com.rentaherramientas.application.billing.usecase.RechazarPagoService;
import com.rentaherramientas.application.billing.usecase.RechazarPagoUseCase;
import com.rentaherramientas.application.shared.port.UnidadDeTrabajo;
import com.rentaherramientas.domain.billing.port.repository.FacturaRepository;
import com.rentaherramientas.domain.billing.port.repository.PagoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Ensambla los casos de uso de billing. Los que aprueban pagos reciben la
 * UnidadDeTrabajo para guardar pago y factura en una misma transacción.
 */
@Configuration
public class BillingBeansConfig {

    /** Bean del caso de uso de pagar reservas. */
    @Bean
    PagarReservaUseCase pagarReservaUseCase(PagoRepository pagoRepository, FacturaRepository facturaRepository,
                                            ConsultaReservas consultaReservas,
                                            GeneradorNumeroFactura generadorNumeroFactura,
                                            UnidadDeTrabajo unidadDeTrabajo, Clock relojNegocio) {
        return new PagarReservaService(pagoRepository, facturaRepository, consultaReservas, generadorNumeroFactura,
                unidadDeTrabajo, relojNegocio);
    }

    /** Bean del caso de uso de confirmar pagos. */
    @Bean
    ConfirmarPagoUseCase confirmarPagoUseCase(PagoRepository pagoRepository, FacturaRepository facturaRepository,
                                              ConsultaReservas consultaReservas,
                                              GeneradorNumeroFactura generadorNumeroFactura,
                                              UnidadDeTrabajo unidadDeTrabajo, Clock relojNegocio) {
        return new ConfirmarPagoService(pagoRepository, facturaRepository, consultaReservas, generadorNumeroFactura,
                unidadDeTrabajo, relojNegocio);
    }

    /** Bean del caso de uso de rechazar pagos. */
    @Bean
    RechazarPagoUseCase rechazarPagoUseCase(PagoRepository pagoRepository, ConsultaReservas consultaReservas) {
        return new RechazarPagoService(pagoRepository, consultaReservas);
    }

    /** Bean del caso de uso de consultar pagos. */
    @Bean
    ConsultarPagosUseCase consultarPagosUseCase(PagoRepository pagoRepository, FacturaRepository facturaRepository,
                                                ConsultaReservas consultaReservas) {
        return new ConsultarPagosService(pagoRepository, facturaRepository, consultaReservas);
    }

    /** Bean del caso de uso de consultar facturas y generar su PDF. */
    @Bean
    ConsultarFacturasUseCase consultarFacturasUseCase(FacturaRepository facturaRepository,
                                                      PagoRepository pagoRepository,
                                                      ConsultaReservas consultaReservas,
                                                      ConsultaTerceros consultaTerceros,
                                                      GeneradorPdfFactura generadorPdfFactura) {
        return new ConsultarFacturasService(facturaRepository, pagoRepository, consultaReservas, consultaTerceros,
                generadorPdfFactura);
    }
}
