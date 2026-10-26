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

    /** Texto Markdown que Swagger UI muestra en la cabecera: cómo autenticarse y con qué usuarios probar. */
    private static final String DESCRIPCION = """
            API REST para el alquiler de herramientas entre proveedores y clientes.

            ### Cómo probar los endpoints protegidos
            1) **POST /api/auth/login** con el email y la contraseña de un usuario de prueba.
            2) **Copia el token** del campo `token` de la respuesta.
            3) Pulsa el botón **Authorize** → pega el token **sin** el prefijo `Bearer` (Swagger lo añade solo).

            ### Usuarios de prueba (perfil dev)
            Contraseña de todos: `Clave123*`

            | Rol | Email |
            |---|---|
            | ADMIN | `admin@rentaherramientas.co` |
            | PROVEEDOR | `carlos.rueda@ferreteriaeltornillo.co` |
            | PROVEEDOR | `ventas@herramientasdeloriente.co` |
            | CLIENTE | `andres.rueda@gmail.com` |
            | CLIENTE | `mcamila.ortiz@hotmail.com` |
            """;

    /** Metadatos de la API y esquema de seguridad Bearer JWT que muestra Swagger UI (/swagger-ui.html). */
    @Bean
    OpenAPI rentaHerramientasOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Renta de Herramientas API")
                        .version("1.0")
                        .description(DESCRIPCION))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME));
    }
}
