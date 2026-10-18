package com.rentaherramientas.infrastructure.catalog.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Modelo de persistencia de la tabla "categorias". No tiene relación JPA con
 * HerramientaEntity: son agregados distintos y se vinculan solo por id.
 */
@Entity
@Table(name = "categorias")
public class CategoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 80, unique = true)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    /** Constructor sin argumentos exigido por JPA. */
    protected CategoriaEntity() {
        // requerido por JPA
    }

    /** Crea la entidad a partir del agregado; un id null indica INSERT. */
    public CategoriaEntity(Long id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    /** Id de la categoría. */
    public Long getId() {
        return id;
    }

    /** Nombre de la categoría. */
    public String getNombre() {
        return nombre;
    }

    /** Descripción de la categoría; puede ser null. */
    public String getDescripcion() {
        return descripcion;
    }
}
