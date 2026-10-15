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

    protected ProveedorEntity() {
        // requerido por JPA
    }

    public ProveedorEntity(Long usuarioId, String razonSocial, String nit) {
        this.usuarioId = usuarioId;
        this.razonSocial = razonSocial;
        this.nit = nit;
    }

    void setUsuario(UsuarioEntity usuario) {
        this.usuario = usuario;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public String getNit() {
        return nit;
    }
}
