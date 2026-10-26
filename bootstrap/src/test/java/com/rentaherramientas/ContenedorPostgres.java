package com.rentaherramientas;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * PostgreSQL real (misma versión que docker-compose) para las pruebas de
 * integración. Con @ServiceConnection, Spring Boot toma la URL, el usuario y la
 * contraseña del contenedor: no hace falta configurar spring.datasource.
 * Al declararlo como bean, todas las pruebas que comparten el contexto de Spring
 * reutilizan el mismo contenedor, y Flyway aplica las migraciones al arrancar.
 */
@TestConfiguration(proxyBeanMethods = false)
public class ContenedorPostgres {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
        return new PostgreSQLContainer("postgres:17");
    }
}
