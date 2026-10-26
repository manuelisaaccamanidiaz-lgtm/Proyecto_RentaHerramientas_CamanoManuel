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
 *
 * TÉCNICA AVANZADA (PostgreSQL): UNIQUE `DEFERRABLE INITIALLY DEFERRED` (V2)
 *   Qué hace: la unicidad de (herramienta_id, orden) se comprueba al hacer COMMIT y no tras cada sentencia.
 *     Hace falta porque Hibernate ejecuta los INSERT antes que los DELETE: si en una transacción se quita la
 *     imagen 2 y se añade otra en la posición 2, a mitad del proceso habría dos filas con orden 2.
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

    /** Constructor sin argumentos exigido por JPA. */
    protected HerramientaImagenEntity() {
        // requerido por JPA
    }

    /** Crea la entidad; el padre se enlaza después con setHerramienta. */
    public HerramientaImagenEntity(Long id, String url, Integer orden) {
        this.id = id;
        this.url = url;
        this.orden = orden;
    }

    /** Enlaza la imagen con su herramienta; lo llama HerramientaEntity.agregarImagen. */
    void setHerramienta(HerramientaEntity herramienta) {
        this.herramienta = herramienta;
    }

    /** Id de la imagen. */
    public Long getId() {
        return id;
    }

    /** URL de la imagen. */
    public String getUrl() {
        return url;
    }

    /** Posición de la imagen en la galería. */
    public Integer getOrden() {
        return orden;
    }
}
