package com.rentaherramientas.infrastructure.rental.config;

import com.rentaherramientas.application.rental.port.CatalogoHerramientas;
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
import java.time.ZoneId;

/**
 * Ensambla los casos de uso de rental y habilita las tareas programadas
 * (vencimiento de reservas). El "hoy" del negocio es el de Colombia: los
 * servicios reciben un Clock en esa zona, que en las pruebas se sustituye por
 * uno fijo.
 */
@Configuration
@EnableScheduling
public class RentalBeansConfig {

    public static final String ZONA_NEGOCIO = "America/Bogota";

    @Bean
    Clock relojNegocio() {
        return Clock.system(ZoneId.of(ZONA_NEGOCIO));
    }

    @Bean
    CrearReservaUseCase crearReservaUseCase(ReservaRepository reservaRepository,
                                            CatalogoHerramientas catalogoHerramientas, Clock relojNegocio) {
        return new CrearReservaService(reservaRepository, catalogoHerramientas, relojNegocio);
    }

    @Bean
    AceptarReservaUseCase aceptarReservaUseCase(ReservaRepository reservaRepository,
                                                CatalogoHerramientas catalogoHerramientas) {
        return new AceptarReservaService(reservaRepository, catalogoHerramientas);
    }

    @Bean
    RechazarReservaUseCase rechazarReservaUseCase(ReservaRepository reservaRepository,
                                                  CatalogoHerramientas catalogoHerramientas) {
        return new RechazarReservaService(reservaRepository, catalogoHerramientas);
    }

    @Bean
    RegistrarEntregaUseCase registrarEntregaUseCase(ReservaRepository reservaRepository,
                                                    CatalogoHerramientas catalogoHerramientas, Clock relojNegocio) {
        return new RegistrarEntregaService(reservaRepository, catalogoHerramientas, relojNegocio);
    }

    @Bean
    RegistrarDevolucionUseCase registrarDevolucionUseCase(ReservaRepository reservaRepository,
                                                          CatalogoHerramientas catalogoHerramientas,
                                                          Clock relojNegocio) {
        return new RegistrarDevolucionService(reservaRepository, catalogoHerramientas, relojNegocio);
    }

    @Bean
    ReportarDanioUseCase reportarDanioUseCase(ReservaRepository reservaRepository,
                                              CatalogoHerramientas catalogoHerramientas) {
        return new ReportarDanioService(reservaRepository, catalogoHerramientas);
    }

    @Bean
    ConsultarReservasUseCase consultarReservasUseCase(ReservaRepository reservaRepository,
                                                      CatalogoHerramientas catalogoHerramientas,
                                                      Clock relojNegocio) {
        return new ConsultarReservasService(reservaRepository, catalogoHerramientas, relojNegocio);
    }

    @Bean
    VencerReservasUseCase vencerReservasUseCase(ReservaRepository reservaRepository, Clock relojNegocio) {
        return new VencerReservasService(reservaRepository, relojNegocio);
    }
}
