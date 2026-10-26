package com.rentaherramientas.infrastructure.identity.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Tabla "proveedores": extensión 1:1 de usuarios cuya PK es también la FK
 * (usuario_id). @MapsId hace que Hibernate copie el id del usuario al insertar,
 * por eso no hay secuencia propia. No declara cascada: la gobierna el padre.
 *
 * TÉCNICA AVANZADA (PostgreSQL): CHECK con expresión regular (V1)
 *   Qué hace: `CHECK (nit ~ '^[0-9]{6,10}(-[0-9])?$')` usa el operador `~` (coincide con la regex) para que
 *     la base de datos rechace NIT mal formados aunque alguien inserte sin pasar por la aplicación. El
 *     dominio valida la misma regla (DatosProveedor).
 */
@Entity
@Table(name = "proveedores")
public class ProveedorEntity {

    @Id
    @Column(name = "usuario_id")
    private Long usuarioId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private UsuarioEntity usuario;

    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Column(name = "nit", nullable = false, length = 20, unique = true)
    private String nit;

    /** Constructor sin argumentos exigido por JPA. */
    protected ProveedorEntity() {
        // requerido por JPA
    }

    /** Crea la entidad con los datos del proveedor; el usuario se enlaza después con setUsuario. */
    public ProveedorEntity(Long usuarioId, String razonSocial, String nit) {
        this.usuarioId = usuarioId;
        this.razonSocial = razonSocial;
        this.nit = nit;
    }

    /** Enlaza el lado hijo de la relación; lo llama UsuarioEntity.asignarProveedor. */
    void setUsuario(UsuarioEntity usuario) {
        this.usuario = usuario;
    }

    /** Id del usuario, que también es la PK de esta tabla. */
    public Long getUsuarioId() {
        return usuarioId;
    }

    /** Razón social del proveedor. */
    public String getRazonSocial() {
        return razonSocial;
    }

    /** NIT del proveedor. */
    public String getNit() {
        return nit;
    }
}
