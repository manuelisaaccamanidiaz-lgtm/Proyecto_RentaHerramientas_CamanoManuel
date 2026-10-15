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
 * Tabla "clientes": extensión 1:1 de usuarios cuya PK es también la FK
 * (usuario_id), igual que ProveedorEntity. @MapsId hace que Hibernate copie el
 * id del usuario al insertar. No declara cascada: la gobierna el padre.
 */
@Entity
@Table(name = "clientes")
public class ClienteEntity {

    @Id
    @Column(name = "usuario_id")
    private Long usuarioId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private UsuarioEntity usuario;

    @Column(name = "documento", nullable = false, length = 20, unique = true)
    private String documento;

    @Column(name = "telefono", nullable = false, length = 20)
    private String telefono;

    @Column(name = "direccion", nullable = false, length = 200)
    private String direccion;

    protected ClienteEntity() {
        // requerido por JPA
    }

    public ClienteEntity(Long usuarioId, String documento, String telefono, String direccion) {
        this.usuarioId = usuarioId;
        this.documento = documento;
        this.telefono = telefono;
        this.direccion = direccion;
    }

    void setUsuario(UsuarioEntity usuario) {
        this.usuario = usuario;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getDocumento() {
        return documento;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getDireccion() {
        return direccion;
    }
}
