package com.rentaherramientas.domain.catalog.model.aggregate;

import com.rentaherramientas.domain.catalog.event.HerramientaEstadoCambiado;
import com.rentaherramientas.domain.catalog.exception.HerramientaSinCategoriaException;
import com.rentaherramientas.domain.catalog.exception.ImagenDuplicadaException;
import com.rentaherramientas.domain.catalog.exception.LimiteImagenesExcedidoException;
import com.rentaherramientas.domain.catalog.exception.TarifaInvalidaException;
import com.rentaherramientas.domain.catalog.exception.TransicionEstadoInvalidaException;
import com.rentaherramientas.domain.catalog.model.valueobject.EstadoHerramienta;
import com.rentaherramientas.domain.catalog.model.valueobject.UrlImagen;
import com.rentaherramientas.domain.shared.model.Validaciones;
import com.rentaherramientas.domain.shared.model.aggregate.AggregateRoot;
import com.rentaherramientas.domain.shared.model.valueobject.Dinero;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Raíz del agregado Herramienta. Cada instancia representa UNA unidad física
 * alquilable. Protege: tarifa positiva, al menos una categoría, orden de imágenes
 * único, un máximo de imágenes y la máquina de estados. El proveedor (BC identity) y las categorías
 * (otro agregado) se referencian solo por id para no acoplar agregados.
 */
public final class Herramienta extends AggregateRoot {

    private static final int NOMBRE_MAX = 120;
    private static final int DESCRIPCION_MAX = 2000;
    public static final int MAX_IMAGENES = 5;
    private static final int MARCA_MAX=60;

    private final Long id;
    private final Long proveedorId;
    private String nombre;
    private String descripcion;
    private String marca;
    private Dinero tarifaDiaria;
    private EstadoHerramienta estado;
    private final List<ImagenHerramienta> imagenes;
    private final Set<Long> categoriaIds;

    /**
     * Constructor privado: valida todos los datos, añade las imágenes una a una y exige al menos una categoría.
     *
     * TÉCNICA AVANZADA (Java): referencia a método
     *   Qué hace: `forEach(this::anexarImagen)` equivale a `forEach(img -> this.anexarImagen(img))`; así cada
     *     imagen pasa por las mismas validaciones que al añadirla después. `LinkedHashSet` evita categorías
     *     repetidas y conserva el orden de inserción.
     */
    private Herramienta(Long id, Long proveedorId, String nombre, String descripcion, String marca, Dinero tarifaDiaria,
                        EstadoHerramienta estado, List<ImagenHerramienta> imagenes, Set<Long> categoriaIds) {
        this.id = id;
        this.proveedorId = Validaciones.requerido(proveedorId, "proveedorId");
        this.nombre = Validaciones.textoRequerido(nombre, "nombre", NOMBRE_MAX);
        this.descripcion = Validaciones.textoOpcional(descripcion, "descripcion", DESCRIPCION_MAX);
        this.marca = Validaciones.textoOpcional(marca, marca, MARCA_MAX);
        this.tarifaDiaria = validarTarifa(tarifaDiaria);
        this.estado = Validaciones.requerido(estado, "estado");
        this.imagenes = new ArrayList<>();
        Validaciones.requerido(imagenes, "imagenes").forEach(this::anexarImagen);
        this.categoriaIds = new LinkedHashSet<>(Validaciones.requerido(categoriaIds, "categoriaIds"));
        if (this.categoriaIds.isEmpty() || this.categoriaIds.contains(null)) {
            throw new HerramientaSinCategoriaException();
        }
    }

    /** Alta de una nueva unidad: siempre empieza DISPONIBLE y sin imágenes. */
    public static Herramienta registrar(Long proveedorId, String nombre, String descripcion, String marca, 
                                        Dinero tarifaDiaria, Set<Long> categoriaIds) {
        return new Herramienta(null, proveedorId, nombre, descripcion, marca, tarifaDiaria,
                EstadoHerramienta.DISPONIBLE, List.of(), categoriaIds);
    }

    /** Reconstruye una herramienta leída de la base de datos; no emite eventos. */
    public static Herramienta reconstruir(Long id, Long proveedorId, String nombre, String descripcion, String marca,
                                          Dinero tarifaDiaria, EstadoHerramienta estado,
                                          List<ImagenHerramienta> imagenes, Set<Long> categoriaIds) {
        return new Herramienta(Validaciones.requerido(id, "id"), proveedorId, nombre, descripcion, marca,
                tarifaDiaria, estado, imagenes, categoriaIds);
    }

    // ---------------------------------------------------------------- datos

    /** Actualiza nombre y descripción con las mismas reglas que al crearla. */
    public void actualizarDatos(String nuevoNombre, String nuevaDescripcion, String nuevaMarca) {
        this.nombre = Validaciones.textoRequerido(nuevoNombre, "nombre", NOMBRE_MAX);
        this.descripcion = Validaciones.textoOpcional(nuevaDescripcion, "descripcion", DESCRIPCION_MAX);
        this.marca = Validaciones.textoOpcional(nuevaMarca, "nuevaMarca", MARCA_MAX);
    }

    /** Cambia la tarifa diaria; debe ser mayor que cero. */
    public void actualizarTarifa(Dinero nuevaTarifa) {
        this.tarifaDiaria = validarTarifa(nuevaTarifa);
    }

    // ------------------------------------------------------------- imágenes

    /** Agrega la imagen al final de la galería (orden = mayor orden actual + 1). */
    public void agregarImagen(UrlImagen url) {
        if (!admiteMasImagenes()) {
            throw new LimiteImagenesExcedidoException(MAX_IMAGENES);
        }
        int siguienteOrden = imagenes.stream().mapToInt(ImagenHerramienta::getOrden).max().orElse(0) + 1;
        anexarImagen(ImagenHerramienta.nueva(url, siguienteOrden));
    }

    /** Indica si aún no se alcanzó el máximo de imágenes permitido. */
    public boolean admiteMasImagenes() {
        return imagenes.size() < MAX_IMAGENES;
    }

    /**
     * Elimina la imagen que ocupa la posición indicada; si no existe, no hace nada.
     *
     * TÉCNICA AVANZADA (Java): `Collection.removeIf`
     *   Qué hace: borra los elementos que cumplen el predicado sin escribir un bucle con `Iterator`, evitando
     *     la `ConcurrentModificationException` de borrar dentro de un for-each.
     */
    public void quitarImagen(int orden) {
        imagenes.removeIf(imagen -> imagen.getOrden() == orden);
    }

    /** Añade una imagen comprobando que su posición (orden) no esté repetida. */
    private void anexarImagen(ImagenHerramienta imagen) {
        Validaciones.requerido(imagen, "imagen");
        if (imagenes.stream().anyMatch(existente -> existente.getOrden() == imagen.getOrden())) {
            throw new ImagenDuplicadaException(imagen.getOrden());
        }
        imagenes.add(imagen);
    }

    // ----------------------------------------------------------- categorías

    /** Asocia la herramienta a una categoría; si ya estaba asociada no cambia nada (es un Set). */
    public void asignarCategoria(Long categoriaId) {
        categoriaIds.add(Validaciones.requerido(categoriaId, "categoriaId"));
    }

    /** Quita una categoría, salvo que sea la última: una herramienta siempre tiene al menos una. */
    public void quitarCategoria(Long categoriaId) {
        if (categoriaIds.contains(categoriaId) && categoriaIds.size() == 1) {
            throw new HerramientaSinCategoriaException();
        }
        categoriaIds.remove(categoriaId);
    }

    // -------------------------------------------------------------- estados

    /** Pasa la herramienta a EN_MANTENIMIENTO si la transición está permitida. */
    public void marcarEnMantenimiento() {
        cambiarEstado(EstadoHerramienta.EN_MANTENIMIENTO);
    }

    /** Pasa la herramienta a DISPONIBLE si la transición está permitida. */
    public void marcarDisponible() {
        cambiarEstado(EstadoHerramienta.DISPONIBLE);
    }

    /** Pasa la herramienta a DANADA si la transición está permitida. */
    public void marcarDanada() {
        cambiarEstado(EstadoHerramienta.DANADA);
    }

    /** Pasa la herramienta a INACTIVA (baja del catálogo) si la transición está permitida. */
    public void inactivar() {
        cambiarEstado(EstadoHerramienta.INACTIVA);
    }

    /** Solo indica si la unidad está operativa; la disponibilidad por fechas es de rental. */
    public boolean estaDisponible() {
        return estado == EstadoHerramienta.DISPONIBLE;
    }

    /** Punto único de cambio de estado: valida la transición y emite HerramientaEstadoCambiado. */
    private void cambiarEstado(EstadoHerramienta destino) {
        if (!estado.puedeCambiarA(destino)) {
            throw new TransicionEstadoInvalidaException(estado, destino);
        }
        EstadoHerramienta anterior = this.estado;
        this.estado = destino;
        registrarEvento(new HerramientaEstadoCambiado(id, anterior, destino, LocalDateTime.now()));
    }

    /** Exige una tarifa presente y positiva y la devuelve para asignarla directamente. */
    private static Dinero validarTarifa(Dinero tarifa) {
        Validaciones.requerido(tarifa, "tarifaDiaria");
        if (!tarifa.esPositivo()) {
            throw new TarifaInvalidaException();
        }
        return tarifa;
    }

    // -------------------------------------------------------------- lectura

    /** Indica si el usuario dado es el proveedor dueño de la herramienta. */
    public boolean perteneceA(Long usuarioId) {
        return proveedorId.equals(usuarioId);
    }

    /** Identificador de la herramienta; null si aún no se ha guardado. */
    public Long getId() {
        return id;
    }

    /** Id del usuario PROVEEDOR dueño de la herramienta. */
    public Long getProveedorId() {
        return proveedorId;
    }

    /** Nombre de la herramienta. */
    public String getNombre() {
        return nombre;
    }

    /** Descripción opcional; puede ser null. */
    public String getDescripcion() {
        return descripcion;
    }

    /** Precio de alquiler por día. */
    public Dinero getTarifaDiaria() {
        return tarifaDiaria;
    }

    /** Estado actual de la herramienta. */
    public EstadoHerramienta getEstado() {
        return estado;
    }

    /** Copia inmutable ordenada: nadie puede alterar la galería sin pasar por el agregado. */
    public List<ImagenHerramienta> getImagenes() {
        return imagenes.stream().sorted(Comparator.comparingInt(ImagenHerramienta::getOrden)).toList();
    }

    /** Copia inmutable de los ids de categoría: modificarla no altera el agregado. */
    public Set<Long> getCategoriaIds() {
        return Set.copyOf(categoriaIds);
    }

    public String getMarca() {
        return marca;
    }

    
}
