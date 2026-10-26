package com.rentaherramientas.domain.catalog.model.valueobject;

import com.rentaherramientas.domain.shared.exception.ValorInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del value object UrlImagen: qué URLs y rutas acepta y cuáles rechaza
 * (esquemas peligrosos, "..", rutas sin "/" y valores demasiado largos).
 */
class UrlImagenTest {

    @ParameterizedTest(name = "acepta {0}")
    @ValueSource(strings = {
            "https://cdn.example.com/img/taladro.png",
            "http://localhost:8080/uploads/3f2a.png",
            "HTTPS://EXAMPLE.COM/A.JPG"})
    @DisplayName("Acepta URLs absolutas http/https")
    void aceptaAbsolutas(String url) {
        UrlImagen imagen = new UrlImagen(url);
        assertEquals(url, imagen.valor());
        assertFalse(imagen.esRelativa());
    }

    @ParameterizedTest(name = "acepta {0}")
    @ValueSource(strings = {
            "/uploads/3f2a9c1e-7b1d-4c55-9a0e-1f2b3c4d5e6f.png",
            "/img/seed/taladro.svg",
            "/a"})
    @DisplayName("Acepta rutas relativas a la raíz que empiezan con '/'")
    void aceptaRelativas(String ruta) {
        UrlImagen imagen = new UrlImagen(ruta);
        assertEquals(ruta, imagen.valor());
        assertTrue(imagen.esRelativa());
    }

    @Test
    @DisplayName("Recorta espacios alrededor")
    void recortaEspacios() {
        assertEquals("/img/seed/taladro.svg", new UrlImagen("  /img/seed/taladro.svg ").valor());
    }

    @ParameterizedTest(name = "rechaza \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {
            "   ",
            "uploads/sin-barra-inicial.png",
            "//evil.com/x.png",
            "/uploads/../application.yml",
            "/uploads/./x.png",
            "/",
            "/uploads/",
            "/uploads/con espacio.png",
            "/uploads\\x.png",
            "/uploads/x.png?y=1",
            "javascript:alert(1)",
            "file:///etc/passwd",
            "ftp://example.com/x.png",
            "http:///sin-host.png"})
    @DisplayName("Rechaza vacíos, rutas sin '/', protocolo-relativas, con '..', caracteres raros y otros esquemas")
    void rechazaInvalidas(String valor) {
        assertThrows(ValorInvalidoException.class, () -> new UrlImagen(valor));
    }

    @Test
    @DisplayName("Rechaza valores de más de 500 caracteres")
    void rechazaLargas() {
        assertThrows(ValorInvalidoException.class, () -> new UrlImagen("/" + "a".repeat(500)));
    }
}
