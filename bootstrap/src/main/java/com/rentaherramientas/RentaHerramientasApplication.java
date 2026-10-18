package com.rentaherramientas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Punto de entrada de la aplicación. Vive en el módulo bootstrap, el único que
 * conoce todas las capas, para que domain y application sigan libres de Spring.
 * Los paquetes de escaneo se declaran explícitamente para dejar claro dónde se
 * buscan beans, repositorios Spring Data y entidades JPA.
 *
 * TÉCNICA AVANZADA (Spring Boot): escaneo en proyecto multimódulo
 *   Qué hace: `@SpringBootApplication` solo busca componentes en su propio paquete y subpaquetes.
 *     `@EnableJpaRepositories` y `@EntityScan` indican dónde están los repositorios Spring Data y las
 *     entidades JPA, que viven en el módulo infrastructure.
 */
@SpringBootApplication(scanBasePackages = "com.rentaherramientas")
@EnableJpaRepositories(basePackages = "com.rentaherramientas.infrastructure")
@EntityScan(basePackages = "com.rentaherramientas.infrastructure")
public class RentaHerramientasApplication {

    /** Arranca Spring Boot: crea el contexto, aplica las migraciones de Flyway y levanta el servidor web. */
    public static void main(String[] args) {
        SpringApplication.run(RentaHerramientasApplication.class, args);
    }
}
