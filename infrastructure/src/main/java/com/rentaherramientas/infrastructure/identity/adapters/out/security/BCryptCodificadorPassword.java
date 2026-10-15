package com.rentaherramientas.infrastructure.identity.adapters.out.security;

import com.rentaherramientas.application.identity.port.CodificadorPassword;
import com.rentaherramientas.domain.identity.model.valueobject.PasswordHash;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida: implementa el hashing con el PasswordEncoder (BCrypt)
 * declarado en SecurityConfig, para que toda la app use el mismo algoritmo.
 */
@Component
public class BCryptCodificadorPassword implements CodificadorPassword {

    private final PasswordEncoder passwordEncoder;

    public BCryptCodificadorPassword(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PasswordHash codificar(String plano) {
        return new PasswordHash(passwordEncoder.encode(plano));
    }

    @Override
    public boolean coincide(String plano, PasswordHash hash) {
        return passwordEncoder.matches(plano, hash.valor());
    }
}
