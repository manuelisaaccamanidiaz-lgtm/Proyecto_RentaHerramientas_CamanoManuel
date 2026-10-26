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
 *
 * TÉCNICA AVANZADA (JPA): clave primaria compartida con `@MapsId`
 *   Qué hace: la PK de "clientes" es la misma que la de "usuarios". `@MapsId` sobre la relación `@OneToOne`
 *     indica que el valor del `@Id` se toma del usuario asociado. `fetch = LAZY` evita cargar el usuario si
 *     no se usa.
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

    /**
     * Constructor sin argumentos exigido por JPA: Hibernate lo usa para crear la instancia al leer
     * de la BD. Es protected para que el código de la aplicación no lo use.
     */
    protected ClienteEntity() {
        // requerido por JPA
    }

    /** Crea la entidad con los datos del cliente; el usuario se enlaza después con setUsuario. */
    public ClienteEntity(Long usuarioId, String documento, String telefono, String direccion) {
        this.usuarioId = usuarioId;
        this.documento = documento;
        this.telefono = telefono;
        this.direccion = direccion;
    }

    /** Enlaza el lado hijo de la relación; lo llama UsuarioEntity.asignarCliente. */
    void setUsuario(UsuarioEntity usuario) {
        this.usuario = usuario;
    }

    /** Id del usuario, que también es la PK de esta tabla. */
    public Long getUsuarioId() {
        return usuarioId;
    }

    /** Número de documento del cliente. */
    public String getDocumento() {
        return documento;
    }

    /** Teléfono del cliente. */
    public String getTelefono() {
        return telefono;
    }

    /** Dirección del cliente. */
    public String getDireccion() {
        return direccion;
    }
}
