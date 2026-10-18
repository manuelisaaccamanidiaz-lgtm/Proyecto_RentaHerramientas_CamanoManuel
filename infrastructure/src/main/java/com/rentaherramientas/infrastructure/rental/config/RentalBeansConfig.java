package com.rentaherramientas.infrastructure.rental.config;

import com.rentaherramientas.application.rental.port.CatalogoHerramientas;
import com.rentaherramientas.application.rental.port.ConsultaPagos;
import com.rentaherramientas.application.rental.usecase.AceptarReservaService;
import com.rentaherramientas.application.rental.usecase.AceptarReservaUseCase;
import com.rentaherramientas.application.rental.usecase.ConsultarReservasService;
import com.rentaherramientas.application.rental.usecase.ConsultarReservasUseCase;
import com.rentaherramientas.application.rental.usecase.CrearReservaService;
import com.rentaherramientas.application.rental.usecase.CrearReservaUseCase;
import com.rentaherramientas.application.rental.usecase.RechazarReservaService;
import com.rentaherramientas.application.rental.usecase.RechazarReservaUseCase;
import com.rentaherramientas.application.rental.usecase.RegistrarDevolucionService;
import com.rentaherramientas.application.rental.usecase.RegistrarDevolucionUseCase;
import com.rentaherramientas.application.rental.usecase.RegistrarEntregaService;
import com.rentaherramientas.application.rental.usecase.RegistrarEntregaUseCase;
import com.rentaherramientas.application.rental.usecase.ReportarDanioService;
import com.rentaherramientas.application.rental.usecase.ReportarDanioUseCase;
import com.rentaherramientas.application.rental.usecase.VencerReservasService;
import com.rentaherramientas.application.rental.usecase.VencerReservasUseCase;
import com.rentaherramientas.domain.rental.port.repository.ReservaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

/**
 * Ensambla los casos de uso de rental y habilita las tareas programadas
 * (vencimiento de reservas). El Clock del negocio (zona de Bogotá) lo declara
 * RelojConfig y se comparte con los demás BC.
 *
 * Nota: `@EnableScheduling` activa el procesamiento de los métodos `@Scheduled` de toda la aplicación (por
 *   ejemplo, VencimientoReservasJob).
 */
@Configuration
@EnableScheduling
public class RentalBeansConfig {

    /** Bean del caso de uso de crear reservas. */
    @Bean
    CrearReservaUseCase crearReservaUseCase(ReservaRepository reservaRepository,
                                            CatalogoHerramientas catalogoHerramientas, Clock relojNegocio) {
        return new CrearReservaService(reservaRepository, catalogoHerramientas, relojNegocio);
    }

    /** Bean del caso de uso de aceptar reservas. */
    @Bean
    AceptarReservaUseCase aceptarReservaUseCase(ReservaRepository reservaRepository,
                                                CatalogoHerramientas catalogoHerramientas) {
        return new AceptarReservaService(reservaRepository, catalogoHerramientas);
    }

    /** Bean del caso de uso de rechazar reservas. */
    @Bean
    RechazarReservaUseCase rechazarReservaUseCase(ReservaRepository reservaRepository,
                                                  CatalogoHerramientas catalogoHerramientas) {
        return new RechazarReservaService(reservaRepository, catalogoHerramientas);
    }

    /** Bean del caso de uso de registrar entregas. */
    @Bean
    RegistrarEntregaUseCase registrarEntregaUseCase(ReservaRepository reservaRepository,
                                                    CatalogoHerramientas catalogoHerramientas,
                                                    ConsultaPagos consultaPagos, Clock relojNegocio) {
        return new RegistrarEntregaService(reservaRepository, catalogoHerramientas, consultaPagos, relojNegocio);
    }

    /** Bean del caso de uso de registrar devoluciones. */
    @Bean
    RegistrarDevolucionUseCase registrarDevolucionUseCase(ReservaRepository reservaRepository,
                                                          CatalogoHerramientas catalogoHerramientas,
                                                          ConsultaPagos consultaPagos, Clock relojNegocio) {
        return new RegistrarDevolucionService(reservaRepository, catalogoHerramientas, consultaPagos, relojNegocio);
    }

    /** Bean del caso de uso de reportar daños. */
    @Bean
    ReportarDanioUseCase reportarDanioUseCase(ReservaRepository reservaRepository,
                                              CatalogoHerramientas catalogoHerramientas,
                                              ConsultaPagos consultaPagos) {
        return new ReportarDanioService(reservaRepository, catalogoHerramientas, consultaPagos);
    }

    /** Bean del caso de uso de consultar reservas y disponibilidad. */
    @Bean
    ConsultarReservasUseCase consultarReservasUseCase(ReservaRepository reservaRepository,
                                                      CatalogoHerramientas catalogoHerramientas,
                                                      ConsultaPagos consultaPagos, Clock relojNegocio) {
        return new ConsultarReservasService(reservaRepository, catalogoHerramientas, consultaPagos, relojNegocio);
    }

    /** Bean del caso de uso de vencer reservas. */
    @Bean
    VencerReservasUseCase vencerReservasUseCase(ReservaRepository reservaRepository, Clock relojNegocio) {
        return new VencerReservasService(reservaRepository, relojNegocio);
    }
}
