/**
 * Cliente · Mis reservas: filtro por estado, detalle con reportes de daño y pago
 * de las reservas ACEPTADAS (modal con el método de pago).
 */
import { api } from '../api.js';
import { requerirSesion } from '../auth.js';
import { nombreHerramienta } from '../datos.js';
import { montarLayout } from '../layout.js';
import { crearListado, filtrosEnUrl } from '../listado.js';
import {
  abrirModal, badge, badgeEstado, botonAccion, cop, datos, el, fechaHora, modalFormulario, rangoFechas,
  tabla, toast,
} from '../ui.js';

const METODOS = [
  ['TARJETA', 'Tarjeta débito o crédito', 'Pago simulado: se aprueba al instante y recibes tu factura.'],
  ['PSE', 'PSE', 'Pago simulado desde tu banco: se aprueba al instante.'],
  ['EFECTIVO', 'Efectivo', 'Pagas al recoger la herramienta; el proveedor confirma el pago.'],
];

if (requerirSesion('CLIENTE')) {
  montarLayout();
  iniciar();
}

function iniciar() {
  const form = document.getElementById('filtros');
  const filtros = filtrosEnUrl(form);
  /** reservaId → pago vigente (PENDIENTE o APROBADO), para saber si ya se pagó o hay efectivo por confirmar. */
  let pagosVigentes = new Map();

  const cargarPagos = async () => {
    const pagos = await api.get('/pagos/mios', { size: 50 });
    pagosVigentes = new Map();
    for (const pago of pagos.contenido) {
      if (pago.estado !== 'RECHAZADO' && !pagosVigentes.has(pago.reservaId)) {
        pagosVigentes.set(pago.reservaId, pago);
      }
    }
  };

  const listado = crearListado({
    contenedor: document.getElementById('lista'),
    cargar: async (pagina) => {
      const valores = filtros.leer();
      filtros.guardar(valores);
      const [reservas] = await Promise.all([
        api.get('/reservas/mias', { ...valores, page: pagina, size: 10 }),
        cargarPagos(),
      ]);
      return reservas;
    },
    pintar: async (respuesta) => {
      const nombres = await Promise.all(respuesta.contenido.map((r) => nombreHerramienta(r.herramientaId)));
      const filas = respuesta.contenido.map((reserva, i) => ({ reserva, nombre: nombres[i] }));
      return tabla([
        {
          titulo: 'Herramienta',
          celda: ({ reserva, nombre }) => el('a', { href: `/herramienta.html?id=${reserva.herramientaId}`, text: nombre }),
        },
        { titulo: 'Fechas', celda: ({ reserva }) => rangoFechas(reserva.fechaInicio, reserva.fechaFin) },
        { titulo: 'Total', clase: 'num', celda: ({ reserva }) => `${cop(reserva.precioTotal)} · ${reserva.dias} d` },
        { titulo: 'Estado', celda: ({ reserva }) => badgeEstado(reserva.estado) },
        { titulo: 'Pago', celda: ({ reserva }) => estadoPago(reserva) },
        { titulo: 'Acciones', celda: ({ reserva, nombre }) => acciones(reserva, nombre) },
      ], filas, { leyenda: 'Mis reservas' });
    },
    vacio: {
      titulo: 'Aún no tienes reservas',
      texto: 'Busca la herramienta que necesitas en el catálogo y elige tus fechas.',
      accion: { texto: 'Explorar el catálogo', href: '/catalogo.html' },
    },
  });

  function estadoPago(reserva) {
    if (reserva.pagada) {
      return badge('Pagada', 'exito');
    }
    const vigente = pagosVigentes.get(reserva.id);
    if (vigente?.estado === 'PENDIENTE') {
      return badge('Efectivo por confirmar', 'advertencia');
    }
    return reserva.estado === 'ACEPTADA' ? badge('Por pagar', 'primario') : el('span', { class: 'texto-suave', text: '—' });
  }

  function acciones(reserva, nombre) {
    const puedePagar = reserva.estado === 'ACEPTADA' && !reserva.pagada && !pagosVigentes.has(reserva.id);
    return el('div', { class: 'acciones' },
      botonAccion('Detalle', 'btn--fantasma', async () => verDetalle(reserva, nombre), { 'aria-label': `Ver detalle de la reserva de ${nombre}` }),
      puedePagar ? botonAccion('Pagar', 'btn--primario', async () => pagar(reserva, nombre), { 'aria-label': `Pagar la reserva de ${nombre}` }) : null);
  }

  function verDetalle(reserva, nombre) {
    abrirModal({
      titulo: `Reserva #${reserva.id}`,
      ancho: 640,
      contenido: el('div', { class: 'pila' },
        datos([
          ['Herramienta', nombre],
          ['Estado', badgeEstado(reserva.estado)],
          ['Fechas', rangoFechas(reserva.fechaInicio, reserva.fechaFin)],
          ['Días', String(reserva.dias)],
          ['Total', cop(reserva.precioTotal)],
          ['Pago', reserva.pagada ? 'Pagada' : 'Sin pago aprobado'],
          ['Entregada', fechaHora(reserva.fechaEntrega)],
          ['Devuelta', fechaHora(reserva.fechaDevolucion)],
          ['Solicitada', fechaHora(reserva.creadoEn)],
        ]),
        reserva.reportesDanio.length
          ? el('section', { class: 'pila' },
            el('h3', { text: 'Reportes de daño' }),
            el('ul', { class: 'lista-simple' }, reserva.reportesDanio.map((r) => el('li', {},
              el('strong', { text: r.descripcion }),
              el('span', { class: 'texto-suave', text: ` · ${fechaHora(r.reportadoEn)}${r.costoEstimado != null ? ` · costo estimado ${cop(r.costoEstimado)}` : ''}` })))))
          : null),
      acciones: [{ texto: 'Cerrar', clase: 'btn--secundario' }],
    });
  }

  async function pagar(reserva, nombre) {
    const opciones = el('fieldset', { class: 'campo' },
      el('legend', { text: 'Método de pago' }),
      el('div', { class: 'opciones-tarjeta' }, METODOS.map(([valor, titulo, ayuda], i) =>
        el('label', { class: 'opcion-tarjeta' },
          el('input', { type: 'radio', name: 'metodo', value: valor, checked: i === 0, required: true }),
          el('span', {}, el('strong', { text: titulo }), el('span', { class: 'texto-sm texto-suave', text: ayuda }))))));
    const resumen = el('div', { class: 'resumen-reserva' },
      el('div', {}, el('span', { text: nombre }), el('span', { text: rangoFechas(reserva.fechaInicio, reserva.fechaFin) })),
      el('div', { class: 'total' }, el('span', { text: 'Total a pagar' }), el('span', { text: cop(reserva.precioTotal) })));
    const pago = await modalFormulario({
      titulo: 'Pagar reserva',
      ancho: 640,
      textoEnviar: `Pagar ${cop(reserva.precioTotal)}`,
      campos: [resumen, opciones,
        el('p', { class: 'texto-xs texto-suave', text: 'Pago simulado: nunca te pediremos ni guardaremos datos de tarjeta.' })],
      alEnviar: (valores) => api.post('/pagos', { reservaId: reserva.id, metodo: valores.metodo }),
    });
    if (!pago) {
      return;
    }
    if (pago.estado === 'APROBADO') {
      toast(`Pago aprobado. Tu factura ${pago.factura?.numero ?? ''} ya está disponible en Mis pagos.`, 'exito', 7000);
    } else {
      toast('Pago en efectivo registrado: el proveedor lo confirmará cuando lo reciba.', 'info', 7000);
    }
    await listado.recargar();
  }

  form.addEventListener('change', () => listado.reiniciar());
  form.addEventListener('submit', (evento) => {
    evento.preventDefault();
    listado.reiniciar();
  });
  listado.recargar(0);
}
