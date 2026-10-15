package com.rentaherramientas.domain.catalog.model.aggregate;

import com.rentaherramientas.domain.shared.model.Validaciones;
import com.rentaherramientas.domain.shared.model.aggregate.AggregateRoot;

/**
 * Raíz de agregado independiente de Herramienta: una categoría tiene ciclo de vida
 * propio (se crea antes que las herramientas y sobrevive a ellas), por eso las
 * herramientas solo guardan su id. La unicidad del nombre la garantiza la BD
 * (uq_categorias_nombre) y el caso de uso la verifica antes con el repositorio.
 */
public final class Categoria extends AggregateRoot {

    private static final int NOMBRE_MAX = 80;
    private static final int DESCRIPCION_MAX = 1000;

    private final Long id;
    private String nombre;
    private String descripcion;

    private Categoria(Long id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = Validaciones.textoRequerido(nombre, "nombre", NOMBRE_MAX);
        this.descripcion = Validaciones.textoOpcional(descripcion, "descripcion", DESCRIPCION_MAX);
    }

    public static Categoria crear(String nombre, String descripcion) {
        return new Categoria(null, nombre, descripcion);
    }

    public static Categoria reconstruir(Long id, String nombre, String descripcion) {
        return new Categoria(Validaciones.requerido(id, "id"), nombre, descripcion);
    }

    public void renombrar(String nuevoNombre) {
        this.nombre = Validaciones.textoRequerido(nuevoNombre, "nombre", NOMBRE_MAX);
    }

    public void cambiarDescripcion(String nuevaDescripcion) {
        this.descripcion = Validaciones.textoOpcional(nuevaDescripcion, "descripcion", DESCRIPCION_MAX);
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
