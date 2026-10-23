package com.rentaherramientas.infrastructure.catalog.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Modelo de persistencia de "favoritos". cliente_id y herramienta_id son Long
 * simples (sin @ManyToOne): el cliente es de otro BC y la herramienta, otro agregado.
 *
 * TÉCNICA AVANZADA (JPA): `@EmbeddedId`
 *   Qué hace: mapea la PK compuesta (cliente_id, herramienta_id) de Flyway a una clase
 *     `@Embeddable` que debe implementar `equals`/`hashCode` (Hibernate la usa como clave).
 *   Cómo usarlo: en JPQL se navega por el id, p. ej. `f.id.clienteId`.
 */
@Entity
@Table(name = "favoritos")
public class FavoritoEntity {

    @EmbeddedId
    private Id id;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    /** Constructor sin argumentos exigido por JPA. */
    protected FavoritoEntity() {
        // requerido por JPA
    }

    /** Id compuesto del favorito. */
    public Id getId() {
        return id;
    }

    /** Momento en que el cliente marcó la herramienta. */
    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    /** PK compuesta (cliente_id, herramienta_id). */
    @Embeddable
    public static class Id implements Serializable {

        @Column(name = "cliente_id", nullable = false)
        private Long clienteId;

        @Column(name = "herramienta_id", nullable = false)
        private Long herramientaId;

        /** Constructor sin argumentos exigido por JPA. */
        protected Id() {
            // requerido por JPA
        }

        /** Id del cliente. */
        public Long getClienteId() {
            return clienteId;
        }

        /** Id de la herramienta. */
        public Long getHerramientaId() {
            return herramientaId;
        }

        @Override
        public boolean equals(Object otro) {
            return otro instanceof Id id
                    && Objects.equals(clienteId, id.clienteId)
                    && Objects.equals(herramientaId, id.herramientaId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(clienteId, herramientaId);
        }
    }
}
