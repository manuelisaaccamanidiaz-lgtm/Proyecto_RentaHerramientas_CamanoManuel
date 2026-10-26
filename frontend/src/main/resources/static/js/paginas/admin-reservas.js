/** Admin · Reservas: historial completo con filtros y sus reportes de daño. */
import { api } from '../api.js';
import { requerirSesion } from '../auth.js';
import { nombreHerramienta, nombreUsuario } from '../datos.js';
import { montarLayout } from '../layout.js';
import { crearListado, filtrosEnUrl } from '../listado.js';
import {
  abrirModal, badge, badgeEstado, botonAccion, cop, datos, el, fechaHora, rangoFechas, tabla,
} from '../ui.js';

if (requerirSesion('ADMIN')) {
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
      return api.get('/admin/reservas', { ...valores, page: pagina, size: 10 });
    },
    pintar: async (respuesta) => {
      const filas = await Promise.all(respuesta.contenido.map(async (reserva) => ({
        reserva,
        herramienta: await nombreHerramienta(reserva.herramientaId),
        cliente: await nombreUsuario(reserva.clienteId),
      })));
      return tabla([
        { titulo: '#', celda: ({ reserva }) => `#${reserva.id}` },
        { titulo: 'Herramienta', celda: ({ herramienta }) => herramienta },
        { titulo: 'Cliente', celda: ({ cliente }) => cliente },
        { titulo: 'Fechas', celda: ({ reserva }) => rangoFechas(reserva.fechaInicio, reserva.fechaFin) },
        { titulo: 'Total', clase: 'num', celda: ({ reserva }) => cop(reserva.precioTotal) },
        { titulo: 'Estado', celda: ({ reserva }) => badgeEstado(reserva.estado) },
        { titulo: 'Pago', celda: ({ reserva }) => (reserva.pagada ? badge('Pagada', 'exito') : badge('Sin pago', 'neutro')) },
        {
          titulo: 'Daños',
          celda: ({ reserva }) => (reserva.reportesDanio.length ? badge(`${reserva.reportesDanio.length} reporte(s)`, 'peligro') : '—'),
        },
        { titulo: 'Detalle', celda: (fila) => botonAccion('Ver', 'btn--fantasma', async () => verDetalle(fila), { 'aria-label': `Ver la reserva #${fila.reserva.id}` }) },
      ], filas, { leyenda: 'Historial de reservas' });
    },
    vacio: { titulo: 'No hay reservas con esos filtros', texto: 'Amplía el rango de fechas o quita algún filtro.' },
  });

  function verDetalle({ reserva, herramienta, cliente }) {
    abrirModal({
      titulo: `Reserva #${reserva.id}`,
      ancho: 680,
      contenido: el('div', { class: 'pila' },
        datos([
          ['Herramienta', `${herramienta} (#${reserva.herramientaId})`],
          ['Cliente', `${cliente} (#${reserva.clienteId})`],
          ['Estado', badgeEstado(reserva.estado)],
          ['Fechas', `${rangoFechas(reserva.fechaInicio, reserva.fechaFin)} (${reserva.dias} días)`],
          ['Total', cop(reserva.precioTotal)],
          ['Pago', reserva.pagada ? 'Pagada' : 'Sin pago aprobado'],
          ['Entregada', fechaHora(reserva.fechaEntrega)],
          ['Devuelta', fechaHora(reserva.fechaDevolucion)],
          ['Solicitada', fechaHora(reserva.creadoEn)],
        ]),
        el('section', { class: 'pila' },
          el('h3', { text: 'Reportes de daño' }),
          reserva.reportesDanio.length
            ? el('ul', { class: 'lista-simple' }, reserva.reportesDanio.map((r) => el('li', {},
              el('strong', { text: r.descripcion }),
              el('span', { class: 'texto-suave', text: ` · ${fechaHora(r.reportadoEn)} · ${r.costoEstimado != null ? cop(r.costoEstimado) : 'sin cotizar'}` }))))
            : el('p', { class: 'texto-suave', text: 'Sin reportes de daño.' }))),
      acciones: [{ texto: 'Cerrar', clase: 'btn--secundario' }],
    });
  }

  form.addEventListener('change', (evento) => {
    if (evento.target.tagName === 'SELECT' || evento.target.type === 'date') listado.reiniciar();
  });
  form.addEventListener('submit', (evento) => {
    evento.preventDefault();
    listado.reiniciar();
  });
  document.getElementById('limpiar').addEventListener('click', () => {
    form.reset();
    listado.reiniciar();
  });
  listado.recargar(0);
}
