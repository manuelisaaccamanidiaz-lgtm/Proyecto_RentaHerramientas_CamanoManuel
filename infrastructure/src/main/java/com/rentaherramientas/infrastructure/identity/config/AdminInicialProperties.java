package com.rentaherramientas.infrastructure.identity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades app.admin.* (ADMIN_EMAIL / ADMIN_PASSWORD) para crear el primer
 * administrador. Ambas son opcionales: si falta alguna no se crea nada.
 */
@ConfigurationProperties("app.admin")
public record AdminInicialProperties(String email, String password, String nombre) {

    public AdminInicialProperties {
        if (nombre == null || nombre.isBlank()) {
            nombre = "Administrador";
        }
    }

    public boolean configurado() {
        return email != null && !email.isBlank() && password != null && !password.isBlank();
    }

    @Override
    public String toString() {
        return "AdminInicialProperties[email=" + email + ", password=****, nombre=" + nombre + "]";
    }
}
