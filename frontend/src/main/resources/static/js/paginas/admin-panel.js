/**
 * Admin · Panel: indicadores globales y gráficas de los reportes (ingresos por
 * mes, herramientas más alquiladas, clientes frecuentes, inventario y rentabilidad).
 */
import { api } from '../api.js';
import { requerirSesion } from '../auth.js';
import { graficaBarras, graficaLineas } from '../graficas.js';
import { montarLayout } from '../layout.js';
import { cop, el, esqueleto, estadoError, numero, reemplazar, tabla, textoEstado } from '../ui.js';

if (requerirSesion('ADMIN')) {
  montarLayout();
  cargar();
}

const formatoMes = new Intl.DateTimeFormat('es-CO', { month: 'short', year: '2-digit' });
const mes = (yyyyMm) => {
  const [anio, numeroMes] = yyyyMm.split('-').map(Number);
  return formatoMes.format(new Date(anio, numeroMes - 1, 1));
};
/** Eje en millones/miles para que las etiquetas quepan: "$1,2 M", "$450 mil". */
const copCorto = (valor) => {
  if (valor >= 1_000_000) return `$${(valor / 1_000_000).toLocaleString('es-CO', { maximumFractionDigits: 1 })} M`;
  if (valor >= 1_000) return `$${Math.round(valor / 1_000).toLocaleString('es-CO')} mil`;
  return `$${Math.round(valor)}`;
};
const suma = (mapa) => Object.values(mapa).reduce((a, b) => a + b, 0);
const aSerie = (mapa) => Object.entries(mapa).map(([clave, valor]) => ({ etiqueta: textoEstado(clave), valor }));

function kpi(etiqueta, valor, detalle, variante = '') {
  return el('div', { class: `kpi ${variante}` },
    el('span', { class: 'kpi__etiqueta', text: etiqueta }),
    el('span', { class: 'kpi__valor', text: valor }),
    detalle ? el('span', { class: 'kpi__detalle', text: detalle }) : null);
}

function tarjetaGrafica(id, titulo, contenido, descripcion) {
  return el('section', { class: 'tarjeta', 'aria-labelledby': id },
    el('h2', { class: 'tarjeta__titulo', id, text: titulo }),
    descripcion ? el('p', { class: 'texto-sm texto-suave', style: 'margin:-0.75rem 0 1rem', text: descripcion }) : null,
    contenido);
}

async function cargar() {
  const kpis = document.getElementById('kpis');
  const graficas = document.getElementById('graficas');
  reemplazar(kpis, esqueleto('kpi', 4));
  reemplazar(graficas, esqueleto('filas', 4));
  try {
    const [resumen, ingresos, masAlquiladas, frecuentes, inventario, rentabilidad] = await Promise.all([
      api.get('/admin/reportes/resumen'),
      api.get('/admin/reportes/ingresos'),
      api.get('/admin/reportes/herramientas-mas-alquiladas', { limite: 8 }),
      api.get('/admin/reportes/clientes-frecuentes', { limite: 8 }),
      api.get('/admin/reportes/inventario'),
      api.get('/admin/reportes/rentabilidad', { limite: 8 }),
    ]);

    const reservas = resumen.reservasPorEstado;
    const activas = (reservas.PENDIENTE ?? 0) + (reservas.ACEPTADA ?? 0) + (reservas.ENTREGADA ?? 0);
    reemplazar(kpis, el('div', { class: 'rejilla rejilla-4' },
      kpi('Ingresos totales', cop(resumen.ingresosPorMoneda.COP ?? 0), `${numero(resumen.pagosAprobados)} pagos aprobados`, 'kpi--exito'),
      kpi('Reservas', numero(suma(reservas)), `${numero(activas)} activas · ${numero(reservas.PENDIENTE ?? 0)} por responder`, 'kpi--info'),
      kpi('Usuarios', numero(suma(resumen.usuariosPorRol)),
        `${numero(resumen.usuariosPorRol.CLIENTE ?? 0)} clientes · ${numero(resumen.usuariosPorRol.PROVEEDOR ?? 0)} proveedores`, 'kpi--secundario'),
      kpi('Herramientas', numero(inventario.totalHerramientas), `${numero(inventario.porEstado.DISPONIBLE ?? 0)} disponibles · ${numero(resumen.facturasEmitidas)} facturas emitidas`)));

    reemplazar(graficas,
      el('div', { class: 'pila-lg' },
        tarjetaGrafica('g-ingresos', 'Ingresos por mes',
          graficaLineas(ingresos.filter((i) => i.moneda === 'COP').map((i) => ({ etiqueta: mes(i.mes), valor: Number(i.total) })),
            { formato: cop, formatoEje: copCorto, titulo: 'Ingresos por mes en los últimos 12 meses' }),
          'Pagos aprobados de los últimos 12 meses, en COP.'),
        el('div', { class: 'rejilla rejilla-2' },
          tarjetaGrafica('g-alquiladas', 'Herramientas más alquiladas',
            graficaBarras(masAlquiladas.map((h) => ({ etiqueta: h.nombre, valor: h.alquileres })),
              { formato: (v) => `${numero(v)} alquiler(es)`, titulo: 'Herramientas con más alquileres' }),
            'Reservas entregadas o devueltas.'),
          tarjetaGrafica('g-frecuentes', 'Clientes frecuentes',
            graficaBarras(frecuentes.map((c) => ({ etiqueta: c.nombre, valor: Number(c.totalPagado) })),
              { formato: cop, titulo: 'Clientes que más han pagado', secundaria: true }),
            'Total pagado por los clientes con más alquileres.')),
        el('div', { class: 'rejilla rejilla-2' },
          tarjetaGrafica('g-estado', 'Inventario por estado',
            graficaBarras(aSerie(inventario.porEstado), { formato: numero, titulo: 'Herramientas por estado' })),
          tarjetaGrafica('g-categoria', 'Inventario por categoría',
            graficaBarras(inventario.porCategoria.slice(0, 8).map((c) => ({ etiqueta: c.categoria, valor: c.herramientas })),
              { formato: numero, titulo: 'Herramientas por categoría', secundaria: true }),
            'Las 8 categorías con más herramientas.')),
        el('div', { class: 'rejilla rejilla-2' },
          tarjetaGrafica('g-rentabilidad', 'Rentabilidad por herramienta',
            graficaBarras(rentabilidad.porHerramienta.map((h) => ({ etiqueta: h.nombre, valor: Number(h.ingresos) })),
              { formato: cop, titulo: 'Ingresos por herramienta' })),
          tarjetaGrafica('t-proveedores', 'Rentabilidad por proveedor',
            rentabilidad.porProveedor.length
              ? tabla([
                { titulo: 'Proveedor', celda: (p) => p.razonSocial },
                { titulo: 'Pagos', clase: 'num', celda: (p) => numero(p.pagos) },
                { titulo: 'Ingresos', clase: 'num', celda: (p) => cop(p.ingresos) },
              ], rentabilidad.porProveedor, { leyenda: 'Ingresos por proveedor' })
              : el('p', { class: 'texto-suave texto-sm', text: 'Aún no hay ingresos.' })))));
  } catch (error) {
    reemplazar(kpis, estadoError(error, cargar));
    graficas.replaceChildren();
  }
}
