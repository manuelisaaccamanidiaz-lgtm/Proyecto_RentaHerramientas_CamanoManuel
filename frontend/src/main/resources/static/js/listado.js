/**
 * Controlador de listas paginadas: muestra el esqueleto mientras carga, el
 * estado vacío si no hay resultados, un error con "Reintentar" si falla, y la
 * paginación al final. Cada página solo dice cómo cargar y cómo pintar.
 */
import { esqueleto, estadoError, estadoVacio, paginacion, reemplazar } from './ui.js';

/**
 * opciones: {
 *   contenedor,                 // donde se pinta la lista
 *   cargar(pagina) → Promise<PaginaResponse>,
 *   pintar(respuesta) → Node | Promise<Node>,   // solo se llama si hay contenido
 *   vacio: { titulo, texto, accion },
 *   tipoEsqueleto: 'filas' | 'tarjetas',
 * }
 */
export function crearListado({ contenedor, cargar, pintar, vacio, tipoEsqueleto = 'filas' }) {
  let paginaActual = 0;
  let solicitud = 0;

  async function recargar(pagina = paginaActual) {
    paginaActual = pagina;
    const numero = ++solicitud; // descarta respuestas de cargas anteriores
    contenedor.setAttribute('aria-busy', 'true');
    reemplazar(contenedor, esqueleto(tipoEsqueleto, tipoEsqueleto === 'tarjetas' ? 8 : 5));
    try {
      const respuesta = await cargar(pagina);
      if (numero !== solicitud) {
        return;
      }
      // Si se borró el último elemento de la última página, retrocede una.
      if (respuesta.contenido.length === 0 && pagina > 0 && respuesta.totalElementos > 0) {
        await recargar(pagina - 1);
        return;
      }
      if (respuesta.contenido.length === 0) {
        reemplazar(contenedor, estadoVacio(vacio));
      } else {
        const contenido = await pintar(respuesta);
        if (numero !== solicitud) {
          return;
        }
        reemplazar(contenedor, contenido, paginacion(respuesta, (destino) => {
          recargar(destino);
          contenedor.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }));
      }
    } catch (error) {
      if (numero === solicitud) {
        reemplazar(contenedor, estadoError(error, () => recargar(paginaActual)));
      }
    } finally {
      if (numero === solicitud) {
        contenedor.removeAttribute('aria-busy');
      }
    }
  }

  return {
    recargar,
    /** Vuelve a la primera página (p. ej. al cambiar un filtro). */
    reiniciar: () => recargar(0),
    get pagina() {
      return paginaActual;
    },
  };
}

/** Sincroniza un formulario de filtros con la URL (para compartir y volver atrás). */
export function filtrosEnUrl(form) {
  const params = new URLSearchParams(location.search);
  for (const control of form.elements) {
    if (control.name && params.has(control.name)) {
      control.value = params.get(control.name);
    }
  }
  return {
    leer() {
      const valores = {};
      for (const control of form.elements) {
        if (control.name && control.value !== '') {
          valores[control.name] = control.value.trim();
        }
      }
      return valores;
    },
    guardar(valores) {
      const url = new URL(location.href);
      url.search = new URLSearchParams(valores).toString();
      history.replaceState(null, '', url);
    },
  };
}
