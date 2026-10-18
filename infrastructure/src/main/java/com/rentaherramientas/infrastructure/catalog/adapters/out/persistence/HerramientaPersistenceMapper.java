package com.rentaherramientas.infrastructure.catalog.adapters.out.persistence;

import com.rentaherramientas.domain.catalog.model.aggregate.Herramienta;
import com.rentaherramientas.domain.catalog.model.aggregate.ImagenHerramienta;
import com.rentaherramientas.domain.catalog.model.valueobject.UrlImagen;
import com.rentaherramientas.domain.shared.model.valueobject.Dinero;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Traduce el agregado Herramienta completo (raíz + imágenes + ids de categorías)
 * hacia/desde su modelo JPA. Aplana el VO Dinero en tarifa_diaria + moneda.
 * Conserva los ids de las imágenes existentes para que Hibernate actualice en
 * lugar de borrar e insertar, y elimine las que el dominio quitó (orphanRemoval).
 */
@Component
public class HerramientaPersistenceMapper {

    /** Convierte la entidad (con imágenes y categorías) en el agregado Herramienta. */
    public Herramienta toDomain(HerramientaEntity entity) {
        List<ImagenHerramienta> imagenes = entity.getImagenes().stream()
                .map(imagen -> ImagenHerramienta.reconstruir(
                        imagen.getId(), new UrlImagen(imagen.getUrl()), imagen.getOrden()))
                .toList();
        return Herramienta.reconstruir(
                entity.getId(),
                entity.getProveedorId(),
                entity.getNombre(),
                entity.getDescripcion(),
                Dinero.de(entity.getTarifaDiaria(), entity.getMoneda()),
                entity.getEstado(),
                imagenes,
                entity.getCategoriaIds());
    }

    /** Convierte el agregado en entidad y añade sus imágenes conservando sus ids. */
    public HerramientaEntity toEntity(Herramienta herramienta) {
        HerramientaEntity entity = new HerramientaEntity(
                herramienta.getId(),
                herramienta.getProveedorId(),
                herramienta.getNombre(),
                herramienta.getDescripcion(),
                herramienta.getTarifaDiaria().monto(),
                herramienta.getTarifaDiaria().moneda(),
                herramienta.getEstado(),
                herramienta.getCategoriaIds());
        herramienta.getImagenes().forEach(imagen -> entity.agregarImagen(
                new HerramientaImagenEntity(imagen.getId(), imagen.getUrl().valor(), imagen.getOrden())));
        return entity;
    }
}
