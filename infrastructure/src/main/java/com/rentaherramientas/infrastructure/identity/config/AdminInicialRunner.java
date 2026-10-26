package com.rentaherramientas.infrastructure.identity.config;

import com.rentaherramientas.application.identity.port.CodificadorPassword;
import com.rentaherramientas.domain.identity.model.aggregate.Usuario;
import com.rentaherramientas.domain.identity.model.valueobject.Email;
import com.rentaherramientas.domain.identity.model.valueobject.Rol;
import com.rentaherramientas.domain.identity.port.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Crea el primer ADMIN al arrancar, porque el registro público no permite ese
 * rol. Es idempotente: si el email ya existe no hace nada. El password nunca se
 * escribe en los logs.
 *
 * TÉCNICA AVANZADA (Spring Boot): `ApplicationRunner`
 *   Qué hace: Spring ejecuta el método `run` de todos los beans que implementan esta interfaz justo después
 *     de arrancar el contexto. Sirve para tareas de inicialización como crear datos base.
 *   Cómo usarlo: crea un `@Component` que implemente `ApplicationRunner`.
 */
@Component
public class AdminInicialRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicialRunner.class);
    private static final int PASSWORD_MINIMO = 8;

    private final AdminInicialProperties properties;
    private final UsuarioRepository usuarioRepository;
    private final CodificadorPassword codificadorPassword;

    /** Recibe la configuración, el repositorio y el codificador por constructor. */
    public AdminInicialRunner(AdminInicialProperties properties, UsuarioRepository usuarioRepository,
                              CodificadorPassword codificadorPassword) {
        this.properties = properties;
        this.usuarioRepository = usuarioRepository;
        this.codificadorPassword = codificadorPassword;
    }

    /** Crea el ADMIN inicial si está configurado y aún no existe; falla si la contraseña es corta. */
    @Override
    public void run(ApplicationArguments args) {
        if (!properties.configurado()) {
            log.info("ADMIN_EMAIL/ADMIN_PASSWORD no definidos: no se crea administrador inicial");
            return;
        }
        if (properties.password().length() < PASSWORD_MINIMO) {
            throw new IllegalStateException("ADMIN_PASSWORD debe tener al menos " + PASSWORD_MINIMO + " caracteres");
        }
        Email email = new Email(properties.email());
        if (usuarioRepository.existePorEmail(email)) {
            log.debug("El administrador inicial {} ya existe", email.valor());
            return;
        }
        Usuario admin = Usuario.registrar(properties.nombre(), email,
                codificadorPassword.codificar(properties.password()), Rol.ADMIN, null, null);
        usuarioRepository.guardar(admin);
        log.info("Administrador inicial creado: {}", email.valor());
    }
}
