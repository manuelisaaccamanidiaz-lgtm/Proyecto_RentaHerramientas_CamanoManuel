/** Admin · Categorías: crear, editar y eliminar (una categoría en uso no se puede borrar: 409). */
import { api } from '../api.js';
import { requerirSesion } from '../auth.js';
import { montarLayout } from '../layout.js';
import {
  botonAccion, campo, confirmar, conCarga, el, esqueleto, estadoError, estadoVacio, modalFormulario, reemplazar,
  tabla, toast, toastError,
} from '../ui.js';

if (requerirSesion('ADMIN')) {
  montarLayout();
  iniciar();
}

function iniciar() {
  const contenedor = document.getElementById('lista');

  async function cargar() {
    reemplazar(contenedor, esqueleto('filas', 6));
    try {
      const lista = await api.get('/categorias');
      reemplazar(contenedor, lista.length === 0
        ? estadoVacio({ titulo: 'No hay categorías', texto: 'Crea la primera para que los proveedores clasifiquen sus herramientas.' })
        : tabla([
          { titulo: 'Nombre', celda: (c) => el('strong', { text: c.nombre }) },
          { titulo: 'Descripción', celda: (c) => c.descripcion ?? '—' },
          { titulo: 'Acciones', celda: (c) => el('div', { class: 'acciones' },
            botonAccion('Editar', 'btn--fantasma', () => guardar(c), { 'aria-label': `Editar ${c.nombre}` }),
            botonAccion('Eliminar', 'btn--peligro', () => eliminar(c), { 'aria-label': `Eliminar ${c.nombre}` })) },
        ], lista, { leyenda: 'Categorías' }));
    } catch (error) {
      reemplazar(contenedor, estadoError(error, cargar));
    }
  }

  async function guardar(categoria) {
    const guardada = await modalFormulario({
      titulo: categoria ? `Editar ${categoria.nombre}` : 'Nueva categoría',
      textoEnviar: categoria ? 'Guardar cambios' : 'Crear categoría',
      campos: [
        campo({ etiqueta: 'Nombre', nombre: 'nombre', valor: categoria?.nombre, requerido: true, atributos: { maxlength: 80 } }),
        campo({ etiqueta: 'Descripción', nombre: 'descripcion', tipo: 'textarea', valor: categoria?.descripcion, atributos: { maxlength: 1000 } }),
      ],
      alEnviar: (valores) => (categoria ? api.put(`/categorias/${categoria.id}`, valores) : api.post('/categorias', valores)),
    });
    if (guardada) {
      toast(categoria ? 'Categoría actualizada.' : `Categoría "${guardada.nombre}" creada.`, 'exito');
      await cargar();
    }
  }

  async function eliminar(categoria) {
    const ok = await confirmar({
      titulo: 'Eliminar categoría',
      mensaje: `Se eliminará "${categoria.nombre}". Si alguna herramienta la usa, no se podrá eliminar.`,
      textoConfirmar: 'Eliminar',
      peligro: true,
    });
    if (!ok) {
      return;
    }
    try {
      await api.eliminar(`/categorias/${categoria.id}`);
      toast('Categoría eliminada.', 'exito');
      await cargar();
    } catch (error) {
      toastError(error, error.status === 409 ? 'No se puede eliminar porque está en uso' : undefined);
    }
  }

  document.getElementById('nueva').addEventListener('click', (evento) => conCarga(evento.currentTarget, () => guardar(null)));
  cargar();
}
