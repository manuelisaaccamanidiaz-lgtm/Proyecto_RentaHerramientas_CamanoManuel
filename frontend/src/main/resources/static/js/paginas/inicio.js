/** Página de inicio: hero con buscador, categorías y herramientas destacadas. */
import { api } from '../api.js';
import { categorias } from '../datos.js';
import { montarLayout } from '../layout.js';
import { el, esqueleto, estadoError, estadoVacio, numero, reemplazar, tarjetaHerramienta } from '../ui.js';

montarLayout();

const contenedorCategorias = document.getElementById('categorias');
const contenedorDestacadas = document.getElementById('destacadas');
const datosHero = document.getElementById('hero-datos');

async function cargarCategorias() {
  reemplazar(contenedorCategorias, el('div', { class: 'skeleton skeleton--fila' }));
  try {
    const lista = await categorias();
    reemplazar(contenedorCategorias, lista.length === 0
      ? el('p', { class: 'texto-suave', text: 'Aún no hay categorías.' })
      : el('ul', { class: 'chips', role: 'list' }, lista.map((categoria) => el('li', {},
        el('a', { class: 'chip', href: `/catalogo.html?categoriaId=${encodeURIComponent(categoria.id)}`, text: categoria.nombre })))));
    return lista.length;
  } catch (error) {
    reemplazar(contenedorCategorias, estadoError(error, cargarCategorias));
    return null;
  }
}

async function cargarDestacadas() {
  reemplazar(contenedorDestacadas, esqueleto('tarjetas', 8));
  try {
    const pagina = await api.get('/herramientas', { estado: 'DISPONIBLE', size: 8 });
    reemplazar(contenedorDestacadas, pagina.contenido.length === 0
      ? estadoVacio({ titulo: 'Aún no hay herramientas disponibles', texto: 'Vuelve pronto: los proveedores publican equipos nuevos cada semana.' })
      : el('div', { class: 'rejilla rejilla-4' }, pagina.contenido.map(tarjetaHerramienta)));
    return pagina.totalElementos;
  } catch (error) {
    reemplazar(contenedorDestacadas, estadoError(error, cargarDestacadas));
    return null;
  }
}

const [totalCategorias, totalDisponibles] = await Promise.all([cargarCategorias(), cargarDestacadas()]);
if (totalCategorias !== null && totalDisponibles !== null) {
  reemplazar(datosHero,
    el('span', {}, el('strong', { text: numero(totalDisponibles) }), ' herramientas disponibles'),
    el('span', {}, el('strong', { text: numero(totalCategorias) }), ' categorías'),
    el('span', {}, el('strong', { text: 'Bucaramanga' }), ' y su área metropolitana'));
}
