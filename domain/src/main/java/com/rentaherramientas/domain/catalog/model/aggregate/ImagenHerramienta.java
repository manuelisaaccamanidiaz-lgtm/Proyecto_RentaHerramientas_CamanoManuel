package com.rentaherramientas.domain.catalog.model.aggregate;

import com.rentaherramientas.domain.catalog.model.valueobject.UrlImagen;
import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;
import com.rentaherramientas.domain.shared.model.Validaciones;

/**
 * Entidad hija del agregado Herramienta (tiene id propio en BD, pero no vive fuera
 * de su herramienta). Solo Herramienta puede crear imágenes nuevas, por eso el
 * factory "nueva" es de paquete; "reconstruir" es público para el mapper.
 */
public final class ImagenHerramienta {

    private final Long id;
    private final UrlImagen url;
    private final int orden;

    private ImagenHerramienta(Long id, UrlImagen url, int orden) {
        if (orden < 1) {
            throw new ValorInvalidoException("El orden de la imagen debe ser mayor o igual a 1");
        }
        this.id = id;
        this.url = Validaciones.requerido(url, "url");
        this.orden = orden;
    }

    static ImagenHerramienta nueva(UrlImagen url, int orden) {
        return new ImagenHerramienta(null, url, orden);
    }

    public static ImagenHerramienta reconstruir(Long id, UrlImagen url, int orden) {
        return new ImagenHerramienta(Validaciones.requerido(id, "id"), url, orden);
    }

    public Long getId() {
        return id;
    }

    public UrlImagen getUrl() {
        return url;
    }

    public int getOrden() {
        return orden;
    }
}
