package com.rentaherramientas.infrastructure.identity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades app.admin.* (ADMIN_EMAIL / ADMIN_PASSWORD) para crear el primer
 * administrador. Ambas son opcionales: si falta alguna no se crea nada.
 */
@ConfigurationProperties("app.admin")
public record AdminInicialProperties(String email, String password, String nombre) {

    /** Constructor compacto: usa "Administrador" como nombre si no se configura. */
    public AdminInicialProperties {
        if (nombre == null || nombre.isBlank()) {
            nombre = "Administrador";
        }
    }

    /** Indica si se definieron email y contraseña del administrador inicial. */
    public boolean configurado() {
        return email != null && !email.isBlank() && password != null && !password.isBlank();
    }

    /** Oculta la contraseña para que no aparezca en logs. */
    @Override
    public String toString() {
        return "AdminInicialProperties[email=" + email + ", password=****, nombre=" + nombre + "]";
    }
}
