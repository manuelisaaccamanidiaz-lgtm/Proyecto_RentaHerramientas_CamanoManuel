package com.rentaherramientas.infrastructure.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentación OpenAPI. Declara el esquema "bearer-jwt" y lo aplica a todas las
 * operaciones, así Swagger UI muestra el botón "Authorize" para pegar el token.
 * Los endpoints públicos (/api/auth/**) lo anulan con @SecurityRequirements vacío.
 */
@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME = "bearer-jwt";

    @Bean
    OpenAPI rentaHerramientasOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Renta de Herramientas API")
                        .version("1.0")
                        .description("API REST para el alquiler de herramientas entre proveedores y clientes."))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME));
    }
}
