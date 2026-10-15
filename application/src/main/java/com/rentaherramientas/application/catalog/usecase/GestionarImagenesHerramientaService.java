package com.rentaherramientas.application.catalog.usecase;

import com.rentaherramientas.application.catalog.command.AgregarImagenCommand;
import com.rentaherramientas.application.catalog.dto.HerramientaResponse;
import com.rentaherramientas.application.catalog.exception.ImagenNoEncontradaException;
import com.rentaherramientas.application.catalog.port.AlmacenamientoImagenes;
import com.rentaherramientas.application.shared.command.Solicitante;
import com.rentaherramientas.domain.catalog.exception.LimiteImagenesExcedidoException;
import com.rentaherramientas.domain.catalog.model.aggregate.Herramienta;
import com.rentaherramientas.domain.catalog.model.aggregate.ImagenHerramienta;
import com.rentaherramientas.domain.catalog.model.valueobject.UrlImagen;
import com.rentaherramientas.domain.catalog.port.repository.HerramientaRepository;
import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;

/**
 * Sube y quita imágenes aplicando la regla de propiedad. El archivo se guarda
 * antes que la herramienta (hace falta su URL); si después falla la regla de
 * dominio o la BD, se borra para no dejar archivos huérfanos. Al quitar, el
 * archivo se borra solo cuando la BD ya no lo referencia.
 */
public class GestionarImagenesHerramientaService implements GestionarImagenesHerramientaUseCase {

    private final HerramientaRepository herramientaRepository;
    private final AlmacenamientoImagenes almacenamiento;

    public GestionarImagenesHerramientaService(HerramientaRepository herramientaRepository,
                                               AlmacenamientoImagenes almacenamiento) {
        this.herramientaRepository = herramientaRepository;
        this.almacenamiento = almacenamiento;
    }

    @Override
    public HerramientaResponse agregar(AgregarImagenCommand command) {
        FormatoImagen formato = validarImagen(command.contenido(), command.contentType());
        Herramienta herramienta = PropiedadHerramienta.cargarParaModificar(herramientaRepository,
                command.herramientaId(), command.solicitante());
        // Se comprueba antes de escribir en disco; agregarImagen lo vuelve a validar.
        if (!herramienta.admiteMasImagenes()) {
            throw new LimiteImagenesExcedidoException(Herramienta.MAX_IMAGENES);
        }

        String url = almacenamiento.guardar(command.contenido(), formato.contentType());
        try {
            herramienta.agregarImagen(new UrlImagen(url));
            return HerramientaResponse.desde(herramientaRepository.guardar(herramienta));
        } catch (RuntimeException e) {
            almacenamiento.eliminar(url);
            throw e;
        }
    }

    @Override
    public HerramientaResponse quitar(Long herramientaId, int orden, Solicitante solicitante) {
        Herramienta herramienta = PropiedadHerramienta.cargarParaModificar(herramientaRepository,
                herramientaId, solicitante);
        String url = herramienta.getImagenes().stream()
                .filter(imagen -> imagen.getOrden() == orden)
                .map(ImagenHerramienta::getUrl)
                .map(UrlImagen::valor)
                .findFirst()
                .orElseThrow(() -> new ImagenNoEncontradaException(herramientaId, orden));
        herramienta.quitarImagen(orden);
        HerramientaResponse respuesta = HerramientaResponse.desde(herramientaRepository.guardar(herramienta));
        almacenamiento.eliminar(url);
        return respuesta;
    }

    private static FormatoImagen validarImagen(byte[] contenido, String contentType) {
        if (contenido == null || contenido.length == 0) {
            throw new ValorInvalidoException("El archivo de imagen está vacío");
        }
        FormatoImagen formato = FormatoImagen.desdeContentType(contentType)
                .orElseThrow(() -> new ValorInvalidoException(
                        "Formato no admitido (" + contentType + "): solo image/jpeg, image/png o image/webp"));
        if (!formato.firmaValida(contenido)) {
            throw new ValorInvalidoException("El contenido del archivo no corresponde a " + formato.contentType());
        }
        return formato;
    }
}
