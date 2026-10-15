package com.rentaherramientas.infrastructure.identity.config;

import com.rentaherramientas.application.identity.port.CodificadorPassword;
import com.rentaherramientas.application.identity.port.GeneradorToken;
import com.rentaherramientas.application.identity.usecase.AutenticarUsuarioService;
import com.rentaherramientas.application.identity.usecase.AutenticarUsuarioUseCase;
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
 */
@Configuration
@EnableConfigurationProperties(AdminInicialProperties.class)
public class IdentityBeansConfig {

    @Bean
    RegistrarUsuarioUseCase registrarUsuarioUseCase(UsuarioRepository usuarioRepository,
                                                    CodificadorPassword codificadorPassword) {
        return new RegistrarUsuarioService(usuarioRepository, codificadorPassword);
    }

    @Bean
    AutenticarUsuarioUseCase autenticarUsuarioUseCase(UsuarioRepository usuarioRepository,
                                                      CodificadorPassword codificadorPassword,
                                                      GeneradorToken generadorToken) {
        return new AutenticarUsuarioService(usuarioRepository, codificadorPassword, generadorToken);
    }

    @Bean
    ConsultarUsuarioUseCase consultarUsuarioUseCase(UsuarioRepository usuarioRepository) {
        return new ConsultarUsuarioService(usuarioRepository);
    }
}
