package com.rentaherramientas.infrastructure.notification.config;

import com.rentaherramientas.application.notification.port.ConsultaDestinatarios;
import com.rentaherramientas.application.notification.usecase.GestionarNotificacionesService;
import com.rentaherramientas.application.notification.usecase.GestionarNotificacionesUseCase;
import com.rentaherramientas.application.notification.usecase.NotificarEventoService;
import com.rentaherramientas.application.notification.usecase.NotificarEventoUseCase;
import com.rentaherramientas.domain.notification.port.repository.NotificacionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Ensambla los casos de uso de notification.
 */
@Configuration
public class NotificationBeansConfig {

    /** Bean del caso de uso que convierte eventos en notificaciones. */
    @Bean
    NotificarEventoUseCase notificarEventoUseCase(NotificacionRepository notificacionRepository,
                                                  ConsultaDestinatarios consultaDestinatarios, Clock relojNegocio) {
        return new NotificarEventoService(notificacionRepository, consultaDestinatarios, relojNegocio);
    }

    /** Bean del caso de uso de consultar y marcar notificaciones. */
    @Bean
    GestionarNotificacionesUseCase gestionarNotificacionesUseCase(NotificacionRepository notificacionRepository) {
        return new GestionarNotificacionesService(notificacionRepository);
    }
}
