package com.rentaherramientas.infrastructure.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Reloj del negocio, compartido por todos los BC. El "hoy" de la plataforma es
 * el de Colombia: los servicios reciben este Clock (no llaman a now() sin zona),
 * y en las pruebas se sustituye por uno fijo.
 */
@Configuration
public class RelojConfig {

    public static final String ZONA_NEGOCIO = "America/Bogota";

    @Bean
    Clock relojNegocio() {
        return Clock.system(ZoneId.of(ZONA_NEGOCIO));
    }
}
