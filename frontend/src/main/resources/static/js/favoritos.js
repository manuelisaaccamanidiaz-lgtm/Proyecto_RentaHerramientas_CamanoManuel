/**
 * Favoritos del cliente: el conjunto de ids marcados (se pide una vez por página)
 * y el botón de corazón que marca (PUT) o desmarca (DELETE). Ambas llamadas son
 * idempotentes en el servidor, así que un doble clic o un reintento no rompen nada.
 * Para cualquier otro rol (o sin sesión) no se pinta el botón.
 */
import { api } from './api.js';
import { sesion } from './auth.js';
import { el, toast, toastError } from './ui.js';

const TAMANO_PAGINA = 50; // el máximo que acepta el backend

let idsEnCache = null;

export function esCliente() {
  return sesion()?.rol === 'CLIENTE';
}

/** Ids de las herramientas favoritas del cliente (recorre todas las páginas). Vacío si no es CLIENTE. */
export function idsFavoritos() {
  if (!esCliente()) {
    return Promise.resolve(new Set());
  }
  idsEnCache ??= (async () => {
    const ids = new Set();
    for (let pagina = 0; ; pagina++) {
      const respuesta = await api.get('/favoritos/mios', { page: pagina, size: TAMANO_PAGINA });
      respuesta.contenido.forEach((herramienta) => ids.add(herramienta.id));
      if (pagina + 1 >= respuesta.totalPaginas) {
        return ids;
      }
    }
  })().catch((error) => {
    idsEnCache = null; // que el siguiente intento vuelva a preguntar
    throw error;
  });
  return idsEnCache;
}

/**
 * Botón ❤️/🤍 de una herramienta. aria-pressed dice si está marcada y la etiqueta
 * no cambia (el lector de pantalla anuncia "botón de alternancia, presionado").
 * Devuelve null si no hay que mostrarlo: no es CLIENTE, o la herramienta está
 * INACTIVA y no estaba marcada (marcarla daría 409). alCambiar(marcado) se llama tras guardar.
 */
export function botonFavorito(herramienta, { marcado = false, alCambiar, clase = '' } = {}) {
  if (!esCliente() || (!marcado && herramienta.estado === 'INACTIVA')) {
    return null;
  }
  let estado = marcado;
  const icono = el('span', { 'aria-hidden': 'true' });
  const boton = el('button', {
    class: `btn-favorito ${clase}`.trim(), type: 'button',
    'aria-label': `Favorita: ${herramienta.nombre}`,
  }, icono);

  const pintar = () => {
    icono.textContent = estado ? '❤️' : '🤍';
    boton.setAttribute('aria-pressed', String(estado));
    boton.title = estado ? 'Quitar de favoritos' : 'Agregar a favoritos';
  };
  pintar();

  boton.addEventListener('click', async (evento) => {
    // En la tarjeta del catálogo, el enlace del nombre cubre toda la tarjeta.
    evento.preventDefault();
    evento.stopPropagation();
    const nuevo = !estado;
    boton.disabled = true;
    try {
      if (nuevo) {
        await api.put(`/favoritos/${herramienta.id}`);
      } else {
        await api.eliminar(`/favoritos/${herramienta.id}`);
      }
      estado = nuevo;
      pintar();
      const ids = await idsEnCache?.catch(() => null);
      ids?.[nuevo ? 'add' : 'delete'](herramienta.id);
      toast(nuevo ? `${herramienta.nombre} se agregó a tus favoritos.` : `${herramienta.nombre} se quitó de tus favoritos.`,
        'exito', 3000);
      await alCambiar?.(estado);
    } catch (error) {
      toastError(error, 'No se pudo actualizar tus favoritos');
    } finally {
      boton.disabled = false;
    }
  });
  return boton;
}
