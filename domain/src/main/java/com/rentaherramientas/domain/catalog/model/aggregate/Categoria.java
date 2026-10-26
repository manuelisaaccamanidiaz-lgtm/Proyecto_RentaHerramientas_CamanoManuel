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

    /** Constructor privado: valida nombre obligatorio y descripción opcional. */
    private Categoria(Long id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = Validaciones.textoRequerido(nombre, "nombre", NOMBRE_MAX);
        this.descripcion = Validaciones.textoOpcional(descripcion, "descripcion", DESCRIPCION_MAX);
    }

    /** Crea una categoría nueva, todavía sin id (lo asigna la base de datos al guardar). */
    public static Categoria crear(String nombre, String descripcion) {
        return new Categoria(null, nombre, descripcion);
    }

    /** Reconstruye una categoría leída de la base de datos; aquí el id es obligatorio. */
    public static Categoria reconstruir(Long id, String nombre, String descripcion) {
        return new Categoria(Validaciones.requerido(id, "id"), nombre, descripcion);
    }

    /** Cambia el nombre; la unicidad entre categorías la comprueba el caso de uso. */
    public void renombrar(String nuevoNombre) {
        this.nombre = Validaciones.textoRequerido(nuevoNombre, "nombre", NOMBRE_MAX);
    }

    /** Cambia la descripción; un texto vacío la deja en null. */
    public void cambiarDescripcion(String nuevaDescripcion) {
        this.descripcion = Validaciones.textoOpcional(nuevaDescripcion, "descripcion", DESCRIPCION_MAX);
    }

    /** Identificador de la categoría; null si aún no se ha guardado. */
    public Long getId() {
        return id;
    }

    /** Nombre único de la categoría. */
    public String getNombre() {
        return nombre;
    }

    /** Descripción opcional; puede ser null. */
    public String getDescripcion() {
        return descripcion;
    }
}
