package com.rentaherramientas.infrastructure.reporting.config;

import com.rentaherramientas.application.reporting.port.ConsultasReportes;
import com.rentaherramientas.application.reporting.usecase.ConsultarReportesService;
import com.rentaherramientas.application.reporting.usecase.ConsultarReportesUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Ensambla el caso de uso de reportes.
 */
@Configuration
public class ReportingBeansConfig {

    @Bean
    ConsultarReportesUseCase consultarReportesUseCase(ConsultasReportes consultasReportes, Clock relojNegocio) {
        return new ConsultarReportesService(consultasReportes, relojNegocio);
    }
}
