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

    private final Long id;
    private final Long proveedorId;
    private String nombre;
    private String descripcion;
    private Dinero tarifaDiaria;
    private EstadoHerramienta estado;
    private final List<ImagenHerramienta> imagenes;
    private final Set<Long> categoriaIds;

    private Herramienta(Long id, Long proveedorId, String nombre, String descripcion, Dinero tarifaDiaria,
                        EstadoHerramienta estado, List<ImagenHerramienta> imagenes, Set<Long> categoriaIds) {
        this.id = id;
        this.proveedorId = Validaciones.requerido(proveedorId, "proveedorId");
        this.nombre = Validaciones.textoRequerido(nombre, "nombre", NOMBRE_MAX);
        this.descripcion = Validaciones.textoOpcional(descripcion, "descripcion", DESCRIPCION_MAX);
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
    public static Herramienta registrar(Long proveedorId, String nombre, String descripcion,
                                        Dinero tarifaDiaria, Set<Long> categoriaIds) {
        return new Herramienta(null, proveedorId, nombre, descripcion, tarifaDiaria,
                EstadoHerramienta.DISPONIBLE, List.of(), categoriaIds);
    }

    public static Herramienta reconstruir(Long id, Long proveedorId, String nombre, String descripcion,
                                          Dinero tarifaDiaria, EstadoHerramienta estado,
                                          List<ImagenHerramienta> imagenes, Set<Long> categoriaIds) {
        return new Herramienta(Validaciones.requerido(id, "id"), proveedorId, nombre, descripcion,
                tarifaDiaria, estado, imagenes, categoriaIds);
    }

    // ---------------------------------------------------------------- datos

    public void actualizarDatos(String nuevoNombre, String nuevaDescripcion) {
        this.nombre = Validaciones.textoRequerido(nuevoNombre, "nombre", NOMBRE_MAX);
        this.descripcion = Validaciones.textoOpcional(nuevaDescripcion, "descripcion", DESCRIPCION_MAX);
    }

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

    public boolean admiteMasImagenes() {
        return imagenes.size() < MAX_IMAGENES;
    }

    public void quitarImagen(int orden) {
        imagenes.removeIf(imagen -> imagen.getOrden() == orden);
    }

    private void anexarImagen(ImagenHerramienta imagen) {
        Validaciones.requerido(imagen, "imagen");
        if (imagenes.stream().anyMatch(existente -> existente.getOrden() == imagen.getOrden())) {
            throw new ImagenDuplicadaException(imagen.getOrden());
        }
        imagenes.add(imagen);
    }

    // ----------------------------------------------------------- categorías

    public void asignarCategoria(Long categoriaId) {
        categoriaIds.add(Validaciones.requerido(categoriaId, "categoriaId"));
    }

    public void quitarCategoria(Long categoriaId) {
        if (categoriaIds.contains(categoriaId) && categoriaIds.size() == 1) {
            throw new HerramientaSinCategoriaException();
        }
        categoriaIds.remove(categoriaId);
    }

    // -------------------------------------------------------------- estados

    public void marcarEnMantenimiento() {
        cambiarEstado(EstadoHerramienta.EN_MANTENIMIENTO);
    }

    public void marcarDisponible() {
        cambiarEstado(EstadoHerramienta.DISPONIBLE);
    }

    public void marcarDanada() {
        cambiarEstado(EstadoHerramienta.DANADA);
    }

    public void inactivar() {
        cambiarEstado(EstadoHerramienta.INACTIVA);
    }

    /** Solo indica si la unidad está operativa; la disponibilidad por fechas es de rental. */
    public boolean estaDisponible() {
        return estado == EstadoHerramienta.DISPONIBLE;
    }

    private void cambiarEstado(EstadoHerramienta destino) {
        if (!estado.puedeCambiarA(destino)) {
            throw new TransicionEstadoInvalidaException(estado, destino);
        }
        EstadoHerramienta anterior = this.estado;
        this.estado = destino;
        registrarEvento(new HerramientaEstadoCambiado(id, anterior, destino, LocalDateTime.now()));
    }

    private static Dinero validarTarifa(Dinero tarifa) {
        Validaciones.requerido(tarifa, "tarifaDiaria");
        if (!tarifa.esPositivo()) {
            throw new TarifaInvalidaException();
        }
        return tarifa;
    }

    // -------------------------------------------------------------- lectura

    public boolean perteneceA(Long usuarioId) {
        return proveedorId.equals(usuarioId);
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

    public Dinero getTarifaDiaria() {
        return tarifaDiaria;
    }

    public EstadoHerramienta getEstado() {
        return estado;
    }

    /** Copia inmutable ordenada: nadie puede alterar la galería sin pasar por el agregado. */
    public List<ImagenHerramienta> getImagenes() {
        return imagenes.stream().sorted(Comparator.comparingInt(ImagenHerramienta::getOrden)).toList();
    }

    public Set<Long> getCategoriaIds() {
        return Set.copyOf(categoriaIds);
    }
}
