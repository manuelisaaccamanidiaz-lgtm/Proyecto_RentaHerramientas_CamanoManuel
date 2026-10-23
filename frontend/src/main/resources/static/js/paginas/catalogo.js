/** Catálogo público con filtros (texto, categoría, estado) y paginación, sincronizados con la URL. */
import { api } from '../api.js';
import { categorias } from '../datos.js';
import { botonFavorito, idsFavoritos } from '../favoritos.js';
import { montarLayout } from '../layout.js';
import { crearListado, filtrosEnUrl } from '../listado.js';
import { el, tarjetaHerramienta, toastError } from '../ui.js';

montarLayout();

const form = document.getElementById('filtros');
const selectCategoria = form.elements.categoriaId;
const resultados = document.getElementById('resultados');

// Las categorías se cargan antes de leer la URL para que el select conserve el filtro.
try {
  for (const categoria of await categorias()) {
    selectCategoria.append(el('option', { value: categoria.id, text: categoria.nombre }));
  }
} catch (error) {
  toastError(error, 'No se pudieron cargar las categorías');
}

const filtros = filtrosEnUrl(form);

const listado = crearListado({
  contenedor: resultados,
  tipoEsqueleto: 'tarjetas',
  cargar: (pagina) => {
    const valores = filtros.leer();
    filtros.guardar(valores);
    return api.get('/herramientas', { ...valores, page: pagina, size: 12 });
  },
  pintar: async (respuesta) => {
    // Si fallan los favoritos, el catálogo se muestra igual con todos los corazones vacíos.
    const favoritos = await idsFavoritos().catch(() => new Set());
    return el('div', { class: 'rejilla rejilla-3' }, respuesta.contenido.map((herramienta) => {
      const tarjeta = tarjetaHerramienta(herramienta);
      const corazon = botonFavorito(herramienta, { marcado: favoritos.has(herramienta.id) });
      if (corazon) {
        tarjeta.append(corazon);
      }
      return tarjeta;
    }));
  },
  vacio: {
    titulo: 'No encontramos herramientas',
    texto: 'Prueba con otra palabra, quita algún filtro o explora todas las categorías.',
    accion: { texto: 'Ver todo el catálogo', href: '/catalogo.html' },
  },
});

form.addEventListener('submit', (evento) => {
  evento.preventDefault();
  listado.reiniciar();
});
// Los select filtran al cambiar; el texto, al enviar o tras dejar de escribir.
form.addEventListener('change', (evento) => {
  if (evento.target.tagName === 'SELECT') {
    listado.reiniciar();
  }
});
let espera;
form.elements.texto.addEventListener('input', () => {
  clearTimeout(espera);
  espera = setTimeout(() => listado.reiniciar(), 400);
});
document.getElementById('limpiar').addEventListener('click', () => {
  form.reset();
  listado.reiniciar();
});

listado.recargar(0);
