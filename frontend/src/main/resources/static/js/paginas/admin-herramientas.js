/** Admin · Herramientas: supervisión del catálogo completo y cambio de estado de cualquier herramienta. */
import { api } from '../api.js';
import { requerirSesion } from '../auth.js';
import { categorias, nombreUsuario, TRANSICIONES_HERRAMIENTA } from '../datos.js';
import { montarLayout } from '../layout.js';
import { crearListado, filtrosEnUrl } from '../listado.js';
import {
  badgeEstado, botonAccion, campo, cop, el, imagenHerramienta, modalFormulario, tabla, textoEstado, toast, toastError,
} from '../ui.js';

if (requerirSesion('ADMIN')) {
  montarLayout();
  iniciar();
}

async function iniciar() {
  const form = document.getElementById('filtros');
  try {
    for (const categoria of await categorias()) {
      form.elements.categoriaId.append(el('option', { value: categoria.id, text: categoria.nombre }));
    }
  } catch (error) {
    toastError(error, 'No se pudieron cargar las categorías');
  }
  const filtros = filtrosEnUrl(form);

  const listado = crearListado({
    contenedor: document.getElementById('lista'),
    cargar: (pagina) => {
      const valores = filtros.leer();
      filtros.guardar(valores);
      return api.get('/herramientas', { ...valores, page: pagina, size: 10 });
    },
    pintar: async (respuesta) => {
      const proveedores = await Promise.all(respuesta.contenido.map((h) => nombreUsuario(h.proveedorId)));
      return tabla([
        { titulo: 'Imagen', celda: ({ h }) => imagenHerramienta(h, 'miniatura-tabla') },
        { titulo: 'Herramienta', celda: ({ h }) => el('a', { href: `/herramienta.html?id=${h.id}`, text: h.nombre }) },
        { titulo: 'Proveedor', celda: ({ proveedor }) => proveedor },
        { titulo: 'Tarifa', clase: 'num', celda: ({ h }) => `${cop(h.tarifaDiaria)}/día` },
        { titulo: 'Estado', celda: ({ h }) => badgeEstado(h.estado) },
        { titulo: 'Acciones', celda: ({ h }) => botonAccion('Cambiar estado', 'btn--fantasma', () => cambiarEstado(h), { 'aria-label': `Cambiar estado de ${h.nombre}` }) },
      ], respuesta.contenido.map((h, i) => ({ h, proveedor: proveedores[i] })), { leyenda: 'Herramientas' });
    },
    vacio: { titulo: 'No hay herramientas con esos filtros', texto: 'Cambia el estado o la categoría.' },
  });

  async function cambiarEstado(herramienta) {
    const destinos = TRANSICIONES_HERRAMIENTA[herramienta.estado] ?? [];
    const actualizada = await modalFormulario({
      titulo: `Estado de ${herramienta.nombre}`,
      textoEnviar: 'Cambiar estado',
      campos: [
        el('p', {}, 'Estado actual: ', badgeEstado(herramienta.estado)),
        campo({ etiqueta: 'Nuevo estado', nombre: 'estado', tipo: 'select', requerido: true, opciones: destinos.map((e) => [e, textoEstado(e)]) }),
      ],
      alEnviar: (valores) => api.patch(`/herramientas/${herramienta.id}/estado`, { estado: valores.estado }),
    });
    if (actualizada) {
      toast(`"${actualizada.nombre}": ${textoEstado(actualizada.estado)}.`, 'exito');
      await listado.recargar();
    }
  }

  form.addEventListener('change', (evento) => {
    if (evento.target.tagName === 'SELECT') listado.reiniciar();
  });
  form.addEventListener('submit', (evento) => {
    evento.preventDefault();
    listado.reiniciar();
  });
  listado.recargar(0);
}
