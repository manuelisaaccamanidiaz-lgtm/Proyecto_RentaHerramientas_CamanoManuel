package com.rentaherramientas.infrastructure.identity.config;

import com.rentaherramientas.application.identity.port.ActividadUsuario;
import com.rentaherramientas.application.identity.port.CodificadorPassword;
import com.rentaherramientas.application.identity.port.GeneradorToken;
import com.rentaherramientas.application.identity.usecase.ActualizarUsuarioService;
import com.rentaherramientas.application.identity.usecase.ActualizarUsuarioUseCase;
import com.rentaherramientas.application.identity.usecase.AutenticarUsuarioService;
import com.rentaherramientas.application.identity.usecase.AutenticarUsuarioUseCase;
import com.rentaherramientas.application.identity.usecase.CambiarEstadoUsuarioService;
import com.rentaherramientas.application.identity.usecase.CambiarEstadoUsuarioUseCase;
import com.rentaherramientas.application.identity.usecase.CambiarRolUsuarioService;
import com.rentaherramientas.application.identity.usecase.CambiarRolUsuarioUseCase;
import com.rentaherramientas.application.identity.usecase.ConsultarUsuarioService;
import com.rentaherramientas.application.identity.usecase.ConsultarUsuarioUseCase;
import com.rentaherramientas.application.identity.usecase.RegistrarUsuarioService;
import com.rentaherramientas.application.identity.usecase.RegistrarUsuarioUseCase;
import com.rentaherramientas.domain.identity.port.repository.UsuarioRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ensambla los casos de uso de identity. Los *Service de application son Java
 * puro (sin @Service); aquí se instancian como beans con sus puertos ya
 * resueltos, y el resto de la app depende solo de las interfaces *UseCase.
 *
 * TÉCNICA AVANZADA (Spring): métodos `@Bean`
 *   Qué hace: cada método devuelve un objeto que Spring registra como bean. Los parámetros del método se
 *     inyectan automáticamente con los beans del tipo correspondiente. Así se integran clases que no tienen
 *     anotaciones de Spring.
 */
@Configuration
@EnableConfigurationProperties(AdminInicialProperties.class)
public class IdentityBeansConfig {

    /** Bean del caso de uso de registro. */
    @Bean
    RegistrarUsuarioUseCase registrarUsuarioUseCase(UsuarioRepository usuarioRepository,
                                                    CodificadorPassword codificadorPassword) {
        return new RegistrarUsuarioService(usuarioRepository, codificadorPassword);
    }

    /** Bean del caso de uso de login. */
    @Bean
    AutenticarUsuarioUseCase autenticarUsuarioUseCase(UsuarioRepository usuarioRepository,
                                                      CodificadorPassword codificadorPassword,
                                                      GeneradorToken generadorToken) {
        return new AutenticarUsuarioService(usuarioRepository, codificadorPassword, generadorToken);
    }

    /** Bean del caso de uso de consulta de usuarios. */
    @Bean
    ConsultarUsuarioUseCase consultarUsuarioUseCase(UsuarioRepository usuarioRepository) {
        return new ConsultarUsuarioService(usuarioRepository);
    }

    /** Bean del caso de uso de actualización de usuarios. */
    @Bean
    ActualizarUsuarioUseCase actualizarUsuarioUseCase(UsuarioRepository usuarioRepository) {
        return new ActualizarUsuarioService(usuarioRepository);
    }

    /** Bean del caso de uso de activar y desactivar usuarios. */
    @Bean
    CambiarEstadoUsuarioUseCase cambiarEstadoUsuarioUseCase(UsuarioRepository usuarioRepository) {
        return new CambiarEstadoUsuarioService(usuarioRepository);
    }

    /** Bean del caso de uso de cambio de rol. */
    @Bean
    CambiarRolUsuarioUseCase cambiarRolUsuarioUseCase(UsuarioRepository usuarioRepository,
                                                      ActividadUsuario actividadUsuario) {
        return new CambiarRolUsuarioService(usuarioRepository, actividadUsuario);
    }
}
