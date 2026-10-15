package com.rentaherramientas.infrastructure.identity.adapters.out.persistence;

import com.rentaherramientas.domain.identity.model.valueobject.Rol;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Modelo de persistencia de la tabla "usuarios". Es un espejo plano del esquema
 * de Flyway, separado del agregado Usuario para que las anotaciones JPA no
 * contaminen el dominio. Los datos de proveedor y de cliente se guardan en las
 * tablas 1:1 "proveedores" y "clientes"; la cascada va del padre (usuario) al hijo.
 */
@Entity
@Table(name = "usuarios")
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "email", nullable = false, length = 150, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 20)
    private Rol rol;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProveedorEntity proveedor;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private ClienteEntity cliente;

    protected UsuarioEntity() {
        // requerido por JPA
    }

    public UsuarioEntity(Long id, String nombre, String email, String passwordHash, Rol rol, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.activo = activo;
    }

    /** Mantiene sincronizados ambos lados de la relación 1:1. */
    public void asignarProveedor(ProveedorEntity nuevoProveedor) {
        this.proveedor = nuevoProveedor;
        if (nuevoProveedor != null) {
            nuevoProveedor.setUsuario(this);
        }
    }

    /** Mantiene sincronizados ambos lados de la relación 1:1. */
    public void asignarCliente(ClienteEntity nuevoCliente) {
        this.cliente = nuevoCliente;
        if (nuevoCliente != null) {
            nuevoCliente.setUsuario(this);
        }
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Rol getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public ProveedorEntity getProveedor() {
        return proveedor;
    }

    public ClienteEntity getCliente() {
        return cliente;
    }
}
