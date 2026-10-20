/**
 * Proveedor · Inventario: alta y edición de herramientas, cambio de estado,
 * imágenes (subir con vista previa y quitar) y baja lógica.
 */
import { api, ApiError } from '../api.js';
import { requerirSesion } from '../auth.js';
import { categorias, TRANSICIONES_HERRAMIENTA } from '../datos.js';
import { montarLayout } from '../layout.js';
import { crearListado, filtrosEnUrl } from '../listado.js';
import {
  abrirModal, badgeEstado, botonAccion, campo, confirmar, conCarga, cop, el, imagenHerramienta, modalFormulario,
  reemplazar, tabla, textoEstado, toast, toastError, zonaCarga,
} from '../ui.js';

const MAX_IMAGENES = 5;
const MAX_BYTES = 5 * 1024 * 1024;
const TIPOS_IMAGEN = ['image/jpeg', 'image/png', 'image/webp'];

if (requerirSesion('PROVEEDOR')) {
  montarLayout();
  iniciar();
}

function iniciar() {
  const form = document.getElementById('filtros');
  const filtros = filtrosEnUrl(form);

  const listado = crearListado({
    contenedor: document.getElementById('lista'),
    cargar: (pagina) => {
      const valores = filtros.leer();
      filtros.guardar(valores);
      return api.get('/proveedor/herramientas', { ...valores, page: pagina, size: 10 });
    },
    pintar: (respuesta) => tabla([
      { titulo: 'Imagen', celda: (h) => imagenHerramienta(h, 'miniatura-tabla') },
      { titulo: 'Herramienta', celda: (h) => el('a', { href: `/herramienta.html?id=${h.id}`, text: h.nombre }) },
      { titulo: 'Tarifa', clase: 'num', celda: (h) => `${cop(h.tarifaDiaria)}/día` },
      { titulo: 'Estado', celda: (h) => badgeEstado(h.estado) },
      { titulo: 'Imágenes', clase: 'num', celda: (h) => `${h.imagenes.length}/${MAX_IMAGENES}` },
      { titulo: 'Acciones', celda: (h) => acciones(h) },
    ], respuesta.contenido, { leyenda: 'Mis herramientas' }),
    vacio: {
      titulo: 'Tu inventario está vacío',
      texto: 'Publica tu primera herramienta para que los clientes puedan reservarla.',
    },
  });

  function acciones(herramienta) {
    return el('div', { class: 'acciones' },
      botonAccion('Editar', 'btn--fantasma', () => editar(herramienta), { 'aria-label': `Editar ${herramienta.nombre}` }),
      botonAccion('Estado', 'btn--fantasma', () => cambiarEstado(herramienta), { 'aria-label': `Cambiar estado de ${herramienta.nombre}` }),
      botonAccion('Imágenes', 'btn--fantasma', () => gestionarImagenes(herramienta), { 'aria-label': `Imágenes de ${herramienta.nombre}` }),
      herramienta.estado !== 'INACTIVA'
        ? botonAccion('Dar de baja', 'btn--peligro', () => darDeBaja(herramienta), { 'aria-label': `Dar de baja ${herramienta.nombre}` })
        : null);
  }

  async function camposHerramienta(herramienta = {}) {
    const lista = await categorias();
    const idError = `categorias-error-${crypto.randomUUID().slice(0, 8)}`;
    const seleccionadas = new Set(herramienta.categoriaIds ?? []);
    return [
      campo({ etiqueta: 'Nombre', nombre: 'nombre', valor: herramienta.nombre, requerido: true, atributos: { maxlength: 120 } }),
      campo({ etiqueta: 'Marca', nombre: 'marca', valor: herramienta.marca, requerido:false, atributos: {maxlength: 60}}),
      campo({ etiqueta: 'Descripción', nombre: 'descripcion', tipo: 'textarea', valor: herramienta.descripcion, atributos: { maxlength: 2000 } }),
      campo({
        etiqueta: 'Tarifa diaria (COP)', nombre: 'tarifaDiaria', tipo: 'number', valor: herramienta.tarifaDiaria, requerido: true,
        ayuda: 'Valor por día de alquiler, en pesos.', atributos: { min: 1, step: 1, inputmode: 'numeric' },
      }),
      el('fieldset', { class: 'campo' },
        el('legend', {}, 'Categorías', el('span', { class: 'requerido', 'aria-hidden': 'true', text: ' *' })),
        el('div', { class: 'grupo-campos grupo-campos--2' }, lista.map((categoria) => el('label', { class: 'fila texto-sm' },
          el('input', {
            type: 'checkbox', name: 'categoriaIds', value: categoria.id, 'data-lista': '',
            checked: seleccionadas.has(categoria.id), 'aria-describedby': idError,
          }),
          categoria.nombre))),
        el('p', { class: 'campo__error', id: idError })),
    ];
  }

  /** Cuerpo de la API a partir del formulario; exige al menos una categoría. */
  function cuerpo(valores) {
    if (!valores.categoriaIds?.length) {
      throw new ApiError(400, { detail: 'Revisa los campos marcados.', errores: { categoriaIds: 'Elige al menos una categoría.' } });
    }
    return {
      nombre: valores.nombre,
      descripcion: valores.descripcion,
      marca:valores.marca,
      tarifaDiaria: Number(valores.tarifaDiaria),
      categoriaIds: valores.categoriaIds.map(Number),
    };
  }

  async function crear() {
    const creada = await modalFormulario({
      titulo: 'Nueva herramienta',
      ancho: 680,
      textoEnviar: 'Publicar herramienta',
      campos: await camposHerramienta(),
      alEnviar: (valores) => api.post('/herramientas', cuerpo(valores)),
    });
    if (creada) {
      toast(`"${creada.nombre}" quedó publicada. Ahora agrégale imágenes.`, 'exito');
      await listado.reiniciar();
      await gestionarImagenes(creada);
    }
  }

  async function editar(herramienta) {
    const actualizada = await modalFormulario({
      titulo: `Editar ${herramienta.nombre}`,
      ancho: 680,
      campos: await camposHerramienta(herramienta),
      alEnviar: (valores) => api.put(`/herramientas/${herramienta.id}`, cuerpo(valores)),
    });
    if (actualizada) {
      toast('Cambios guardados.', 'exito');
      await listado.recargar();
    }
  }

  async function cambiarEstado(herramienta) {
    const destinos = TRANSICIONES_HERRAMIENTA[herramienta.estado] ?? [];
    const actualizada = await modalFormulario({
      titulo: 'Cambiar estado',
      textoEnviar: 'Cambiar estado',
      campos: [
        el('p', {}, 'Estado actual: ', badgeEstado(herramienta.estado)),
        campo({ etiqueta: 'Nuevo estado', nombre: 'estado', tipo: 'select', requerido: true, opciones: destinos.map((e) => [e, textoEstado(e)]) }),
        el('p', { class: 'texto-xs texto-suave', text: 'Solo se muestran los cambios permitidos. Una herramienta que no está DISPONIBLE no recibe reservas nuevas.' }),
      ],
      alEnviar: (valores) => api.patch(`/herramientas/${herramienta.id}/estado`, { estado: valores.estado }),
    });
    if (actualizada) {
      toast(`"${actualizada.nombre}" ahora está ${textoEstado(actualizada.estado).toLowerCase()}.`, 'exito');
      await listado.recargar();
    }
  }

  async function darDeBaja(herramienta) {
    const ok = await confirmar({
      titulo: 'Dar de baja la herramienta',
      mensaje: `"${herramienta.nombre}" quedará INACTIVA y dejará de aparecer en el catálogo. Sus reservas e historial se conservan. Podrás reactivarla después.`,
      textoConfirmar: 'Dar de baja',
      peligro: true,
    });
    if (!ok) {
      return;
    }
    try {
      await api.eliminar(`/herramientas/${herramienta.id}`);
      toast('Herramienta dada de baja.', 'exito');
      await listado.recargar();
    } catch (error) {
      toastError(error);
    }
  }

  async function gestionarImagenes(inicial) {
    let herramienta = inicial;
    let cambios = false;
    const contenido = el('div', { class: 'pila' });

    const pintar = () => {
      const lleno = herramienta.imagenes.length >= MAX_IMAGENES;
      const input = el('input', { type: 'file', name: 'archivo', accept: TIPOS_IMAGEN.join(','), id: 'archivo-imagen', 'aria-describedby': 'ayuda-imagen error-imagen' });
      const error = el('p', { class: 'campo__error', id: 'error-imagen', role: 'alert' });
      const subir = el('button', { class: 'btn btn--primario', type: 'button', disabled: true }, el('span', { text: 'Subir imagen' }));

      input.addEventListener('change', () => {
        error.textContent = '';
        input.removeAttribute('aria-invalid');
        subir.disabled = true;
        const archivo = input.files[0];
        if (!archivo) return;
        if (!TIPOS_IMAGEN.includes(archivo.type)) {
          error.textContent = 'Solo se admiten imágenes JPG, PNG o WebP.';
          input.setAttribute('aria-invalid', 'true');
          return;
        }
        if (archivo.size > MAX_BYTES) {
          error.textContent = 'La imagen supera 5 MB.';
          input.setAttribute('aria-invalid', 'true');
          return;
        }
        subir.disabled = false;
      });

      subir.addEventListener('click', () => conCarga(subir, async () => {
        const datosArchivo = new FormData();
        datosArchivo.append('archivo', input.files[0]);
        try {
          herramienta = await api.subir(`/herramientas/${herramienta.id}/imagenes`, datosArchivo);
          cambios = true;
          toast('Imagen agregada.', 'exito');
          pintar();
        } catch (e) {
          error.textContent = e instanceof ApiError ? e.detalle : 'No se pudo subir la imagen.';
        }
      }));

      reemplazar(contenido,
        herramienta.imagenes.length
          ? el('ul', { class: 'imagenes-editor', role: 'list' }, herramienta.imagenes.map((imagen) => {
            const quitar = botonAccion('Quitar', 'btn--peligro', async () => {
              const ok = await confirmar({ titulo: 'Quitar imagen', mensaje: 'La imagen se borrará de la galería. Esta acción no se puede deshacer.', textoConfirmar: 'Quitar', peligro: true });
              if (!ok) return;
              try {
                herramienta = await api.eliminar(`/herramientas/${herramienta.id}/imagenes/${imagen.orden}`);
                cambios = true;
                toast('Imagen quitada.', 'exito');
                pintar();
              } catch (e) {
                toastError(e);
              }
            }, { 'aria-label': `Quitar imagen ${imagen.orden}` });
            return el('li', {}, el('img', { src: imagen.url, alt: `Imagen ${imagen.orden} de ${herramienta.nombre}` }), quitar);
          }))
          : el('p', { class: 'texto-suave', text: 'Esta herramienta aún no tiene imágenes.' }),
        lleno
          ? el('p', { class: 'aviso aviso--info', text: `Llegaste al máximo de ${MAX_IMAGENES} imágenes. Quita una para subir otra.` })
          : el('div', { class: 'campo' },
            el('label', { for: 'archivo-imagen', text: 'Agregar imagen' }),
            zonaCarga(input),
            el('p', { class: 'campo__ayuda', id: 'ayuda-imagen', text: `JPG, PNG o WebP de hasta 5 MB. ${herramienta.imagenes.length}/${MAX_IMAGENES} usadas.` }),
            error,
            el('div', {}, subir)));
    };

    pintar();
    const { resultado } = abrirModal({ titulo: `Imágenes · ${herramienta.nombre}`, contenido, ancho: 680, acciones: [{ texto: 'Listo', clase: 'btn--secundario' }] });
    await resultado;
    if (cambios) {
      await listado.recargar();
    }
  }

  document.getElementById('nueva').addEventListener('click', (evento) => conCarga(evento.currentTarget, crear));
  form.addEventListener('change', () => listado.reiniciar());
  form.addEventListener('submit', (evento) => {
    evento.preventDefault();
    listado.reiniciar();
  });
  listado.recargar(0);
}
