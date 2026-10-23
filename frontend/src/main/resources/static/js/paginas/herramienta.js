/**
 * Ficha de una herramienta: galería, descripción, tarifa, calendario de
 * disponibilidad y reserva con el precio calculado en vivo (tarifa con descuento × días).
 */
import { api, ApiError } from '../api.js';
import { rutaInicio, sesion } from '../auth.js';
import { crearCalendario } from '../calendario.js';
import { categorias } from '../datos.js';
import { montarLayout } from '../layout.js';
import {
  aIso, badgeEstado, conCarga, cop, el, estadoError, estadoVacio, fechaLarga, parametrosUrl, reemplazar,
  precioHerramienta, sumarDias, toastEnSiguientePagina, toastError,
} from '../ui.js';

montarLayout();

const DIAS_VISIBLES = 180;
const MAX_DIAS = 30;
const contenedor = document.getElementById('ficha');
const id = Number(parametrosUrl().id);

function galeria(herramienta) {
  const imagenes = herramienta.imagenes.length
    ? herramienta.imagenes
    : [{ orden: 0, url: '/img/herramienta-generica.svg' }];
  const principal = el('img', {
    class: 'galeria__principal', src: imagenes[0].url, alt: `${herramienta.nombre}, imagen 1`, width: 800, height: 600,
  });
  const miniaturas = imagenes.length > 1
    ? el('ul', { class: 'galeria__miniaturas', role: 'list' }, imagenes.map((imagen, i) => {
      const boton = el('button', {
        class: 'galeria__miniatura', type: 'button', 'aria-label': `Ver imagen ${i + 1}`,
        'aria-current': i === 0 ? 'true' : 'false',
      }, el('img', { src: imagen.url, alt: '', loading: 'lazy' }));
      boton.addEventListener('click', () => {
        principal.src = imagen.url;
        principal.alt = `${herramienta.nombre}, imagen ${i + 1}`;
        for (const otro of boton.closest('ul').querySelectorAll('button')) {
          otro.setAttribute('aria-current', String(otro === boton));
        }
      });
      return el('li', {}, boton);
    }))
    : null;
  return el('div', { class: 'galeria' }, principal, miniaturas);
}

function panelReserva(herramienta) {
  const actual = sesion();
  const hoy = new Date();
  hoy.setHours(0, 0, 0, 0);
  const maximo = sumarDias(hoy, DIAS_VISIBLES);
  let seleccion = null;
  // Misma cuenta que el servidor: tarifa con descuento (ya redondeada a centavos) × días.
  // Se multiplica en centavos enteros para no arrastrar errores de coma flotante.
  const tarifa = Number(herramienta.tarifaConDescuento ?? herramienta.tarifaDiaria);
  const total = (dias) => (Math.round(tarifa * 100) * dias) / 100;

  const aviso = el('div', { class: 'aviso aviso--peligro', role: 'alert' });
  const resumen = el('div', { class: 'resumen-reserva', 'aria-live': 'polite' });
  const boton = el('button', { class: 'btn btn--primario btn--lg btn--bloque', type: 'button' }, el('span', { text: 'Reservar' }));

  const pintarResumen = () => {
    if (!seleccion) {
      reemplazar(resumen, el('p', { class: 'texto-suave', text: 'Elige en el calendario el día de inicio y el día de fin.' }));
      return;
    }
    reemplazar(resumen,
      el('div', {}, el('span', { text: 'Desde' }), el('strong', { text: fechaLarga(seleccion.inicio) })),
      el('div', {}, el('span', { text: 'Hasta' }), el('strong', { text: fechaLarga(seleccion.fin) })),
      el('div', {}, el('span', { text: `${cop(tarifa)} × ${seleccion.dias} día(s)` }),
        el('span', { text: cop(total(seleccion.dias)) })),
      el('div', { class: 'total' }, el('span', { text: 'Total' }), el('span', { text: cop(total(seleccion.dias)) })));
  };

  const calendario = crearCalendario({
    minimo: hoy,
    maximo,
    maxDias: MAX_DIAS,
    alCambiar: (rango) => {
      seleccion = rango;
      aviso.textContent = '';
      pintarResumen();
      actualizarBoton();
    },
    alError: (mensaje) => {
      aviso.textContent = mensaje;
    },
  });

  const cargarDisponibilidad = async () => {
    const disponibilidad = await api.get(`/herramientas/${herramienta.id}/disponibilidad`, {
      desde: aIso(hoy), hasta: aIso(maximo),
    });
    calendario.ponerOcupados(disponibilidad.ocupados);
  };

  // Qué hace el botón según la sesión y el estado de la herramienta.
  let motivo = null;
  if (herramienta.estado !== 'DISPONIBLE') {
    motivo = 'Esta herramienta no está disponible para reservar en este momento.';
  } else if (actual && actual.rol !== 'CLIENTE') {
    motivo = 'Solo las cuentas de cliente pueden hacer reservas.';
  }
  function actualizarBoton() {
    boton.disabled = Boolean(motivo) || (actual && !seleccion);
    boton.querySelector('span').textContent = actual ? 'Reservar' : 'Inicia sesión para reservar';
  }
  actualizarBoton();

  boton.addEventListener('click', () => {
    if (!actual) {
      const volver = `/herramienta.html?id=${herramienta.id}`;
      location.href = `/login.html?siguiente=${encodeURIComponent(volver)}`;
      return;
    }
    conCarga(boton, async () => {
      try {
        await api.post('/reservas', { herramientaId: herramienta.id, fechaInicio: seleccion.inicio, fechaFin: seleccion.fin });
        toastEnSiguientePagina('¡Reserva enviada! El proveedor la revisará pronto.', 'exito');
        location.href = '/cliente/reservas.html';
      } catch (error) {
        aviso.textContent = error instanceof ApiError ? error.detalle : 'No se pudo crear la reserva.';
        if (error instanceof ApiError && error.status === 409) {
          await cargarDisponibilidad().catch(() => {});
        }
      }
    });
  });

  pintarResumen();
  const tarjeta = el('section', { class: 'tarjeta pila', 'aria-labelledby': 'titulo-reserva' },
    el('h2', { id: 'titulo-reserva', text: 'Reserva tus fechas' }),
    el('p', { class: 'texto-suave texto-sm', text: `Máximo ${MAX_DIAS} días por reserva. Ambos días se incluyen.` }),
    calendario.raiz,
    aviso,
    resumen,
    motivo ? el('p', { class: 'aviso aviso--advertencia', text: motivo }) : null,
    actual && actual.rol !== 'CLIENTE'
      ? el('a', { href: rutaInicio(actual.rol), class: 'texto-sm', text: 'Ir a mi panel' })
      : null,
    boton);
  return { tarjeta, cargarDisponibilidad };
}

async function cargar() {
  reemplazar(contenedor, el('div', { class: 'detalle' },
    el('div', { class: 'skeleton skeleton--imagen', style: 'border-radius:16px' }),
    el('div', {}, el('div', { class: 'skeleton skeleton--titulo' }), el('div', { class: 'skeleton skeleton--linea' }),
      el('div', { class: 'skeleton skeleton--linea' }))));
  if (!Number.isInteger(id) || id <= 0) {
    reemplazar(contenedor, estadoVacio({ titulo: 'Herramienta no encontrada', texto: 'El enlace no es válido.', accion: { texto: 'Ir al catálogo', href: '/catalogo.html' } }));
    return;
  }
  try {
    const [herramienta, listaCategorias] = await Promise.all([api.get(`/herramientas/${id}`), categorias().catch(() => [])]);
    document.title = `${herramienta.nombre} · RentaHerramientas`;
    const nombresCategoria = listaCategorias.filter((c) => herramienta.categoriaIds.includes(c.id));
    const { tarjeta, cargarDisponibilidad } = panelReserva(herramienta);

    reemplazar(contenedor,
      el('nav', { class: 'texto-sm', 'aria-label': 'Migas de pan', style: 'margin-bottom:1rem' },
        el('a', { href: '/catalogo.html', text: 'Catálogo' }), ' / ', el('span', { text: herramienta.nombre })),
      el('div', { class: 'detalle' },
        el('div', { class: 'pila' },
          galeria(herramienta),
          el('section', { class: 'tarjeta pila', 'aria-labelledby': 'titulo-descripcion' },
            el('h2', { id: 'titulo-descripcion', text: 'Descripción' }),
            el('p', { text: herramienta.descripcion || 'El proveedor no agregó una descripción.' }),
            herramienta.marca
              ? el('p', { class: 'texto-sm' }, el('strong', { text: 'Marca: ' }), el('span', { text: herramienta.marca }))
              : null,
            nombresCategoria.length
              ? el('ul', { class: 'chips', role: 'list', 'aria-label': 'Categorías' },
                nombresCategoria.map((c) => el('li', {}, el('a', { class: 'chip', href: `/catalogo.html?categoriaId=${c.id}`, text: c.nombre }))))
              : null)),
        el('div', { class: 'pila' },
          el('div', { class: 'pila' },
            badgeEstado(herramienta.estado),
            el('h1', { text: herramienta.nombre }),
            precioHerramienta(herramienta, { style: 'font-size:2rem' })),
          tarjeta)));

    await cargarDisponibilidad().catch((error) => toastError(error, 'No se pudo cargar la disponibilidad'));
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) {
      reemplazar(contenedor, estadoVacio({ titulo: 'Herramienta no encontrada', texto: 'Puede que ya no esté publicada.', accion: { texto: 'Ir al catálogo', href: '/catalogo.html' } }));
    } else {
      reemplazar(contenedor, estadoError(error, cargar));
    }
  }
}

cargar();
