package com.rentaherramientas.infrastructure.catalog.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Tabla "herramienta_imagenes": entidad hija del agregado Herramienta.
 * El @ManyToOne hacia el padre NO lleva cascada; la cascada la define el padre.
 */
@Entity
@Table(name = "herramienta_imagenes")
public class HerramientaImagenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "herramienta_id", nullable = false)
    private HerramientaEntity herramienta;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    protected HerramientaImagenEntity() {
        // requerido por JPA
    }

    public HerramientaImagenEntity(Long id, String url, Integer orden) {
        this.id = id;
        this.url = url;
        this.orden = orden;
    }

    void setHerramienta(HerramientaEntity herramienta) {
        this.herramienta = herramienta;
    }

    public Long getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public Integer getOrden() {
        return orden;
    }
}
