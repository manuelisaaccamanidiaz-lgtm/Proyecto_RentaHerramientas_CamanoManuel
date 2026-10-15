package com.rentaherramientas.infrastructure.catalog.adapters.out.persistence;

import com.rentaherramientas.domain.catalog.model.valueobject.EstadoHerramienta;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Modelo de persistencia de "herramientas".
 * - proveedor_id es un Long simple: el proveedor pertenece a otro BC (identity),
 *   así que no hay @ManyToOne hacia UsuarioEntity.
 * - Las imágenes son entidad hija del mismo agregado: @OneToMany con cascada y
 *   orphanRemoval desde el padre.
 * - Las categorías son otro agregado: se mapea solo la tabla puente
 *   herramienta_categorias como colección de ids (@ElementCollection), cuya PK
 *   compuesta (herramienta_id, categoria_id) coincide con la de Flyway.
 */
@Entity
@Table(name = "herramientas")
public class HerramientaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "proveedor_id", nullable = false)
    private Long proveedorId;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "tarifa_diaria", nullable = false, precision = 12, scale = 2)
    private BigDecimal tarifaDiaria;

    /** CHAR(3) en PostgreSQL: se declara el tipo JDBC para que ddl-auto=validate lo acepte. */
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "moneda", nullable = false, length = 3)
    private String moneda;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoHerramienta estado;

    @OneToMany(mappedBy = "herramienta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<HerramientaImagenEntity> imagenes = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "herramienta_categorias",
            joinColumns = @JoinColumn(name = "herramienta_id"))
    @Column(name = "categoria_id", nullable = false)
    private Set<Long> categoriaIds = new HashSet<>();

    protected HerramientaEntity() {
        // requerido por JPA
    }

    public HerramientaEntity(Long id, Long proveedorId, String nombre, String descripcion,
                             BigDecimal tarifaDiaria, String moneda, EstadoHerramienta estado,
                             Set<Long> categoriaIds) {
        this.id = id;
        this.proveedorId = proveedorId;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tarifaDiaria = tarifaDiaria;
        this.moneda = moneda;
        this.estado = estado;
        this.categoriaIds = new HashSet<>(categoriaIds);
    }

    /** Mantiene sincronizados ambos lados de la relación padre-hijo. */
    public void agregarImagen(HerramientaImagenEntity imagen) {
        imagen.setHerramienta(this);
        imagenes.add(imagen);
    }

    public Long getId() {
        return id;
    }

    public Long getProveedorId() {
        return proveedorId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getTarifaDiaria() {
        return tarifaDiaria;
    }

    public String getMoneda() {
        return moneda;
    }

    public EstadoHerramienta getEstado() {
        return estado;
    }

    public List<HerramientaImagenEntity> getImagenes() {
        return imagenes;
    }

    public Set<Long> getCategoriaIds() {
        return categoriaIds;
    }
}
