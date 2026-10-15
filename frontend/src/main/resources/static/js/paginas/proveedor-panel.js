/** Proveedor · Panel: indicadores de su resumen y gráficas de reservas y herramientas. */
import { api } from '../api.js';
import { requerirSesion } from '../auth.js';
import { graficaBarras } from '../graficas.js';
import { montarLayout } from '../layout.js';
import { cop, el, esqueleto, estadoError, numero, reemplazar, textoEstado } from '../ui.js';

if (requerirSesion('PROVEEDOR')) {
  montarLayout();
  cargar();
}

function kpi(etiqueta, valor, detalle, variante = '') {
  return el('div', { class: `kpi ${variante}` },
    el('span', { class: 'kpi__etiqueta', text: etiqueta }),
    el('span', { class: 'kpi__valor', text: valor }),
    detalle ? el('span', { class: 'kpi__detalle', text: detalle }) : null);
}

const suma = (mapa, claves) => claves.reduce((total, clave) => total + (mapa[clave] ?? 0), 0);
const aSerie = (mapa) => Object.entries(mapa).map(([estado, valor]) => ({ etiqueta: textoEstado(estado), valor }));

async function cargar() {
  const kpis = document.getElementById('kpis');
  const graficas = document.getElementById('graficas');
  reemplazar(kpis, esqueleto('kpi', 4));
  graficas.replaceChildren();
  try {
    const resumen = await api.get('/proveedor/reportes/resumen');
    const pendientes = resumen.reservasPorEstado.PENDIENTE ?? 0;
    const activas = suma(resumen.reservasPorEstado, ['PENDIENTE', 'ACEPTADA', 'ENTREGADA']);
    const totalHerramientas = suma(resumen.herramientasPorEstado, Object.keys(resumen.herramientasPorEstado));
    reemplazar(kpis, el('div', { class: 'rejilla rejilla-4' },
      kpi('Ingresos', cop(resumen.ingresosPorMoneda.COP ?? 0), `${numero(resumen.pagosAprobados)} pago(s) aprobado(s)`, 'kpi--exito'),
      kpi('Solicitudes por responder', numero(pendientes), 'Reservas PENDIENTES', pendientes > 0 ? 'kpi--advertencia' : ''),
      kpi('Reservas activas', numero(activas), 'Pendientes, aceptadas y entregadas', 'kpi--info'),
      kpi('Herramientas', numero(totalHerramientas), `${numero(resumen.herramientasPorEstado.DISPONIBLE ?? 0)} disponibles`, 'kpi--secundario')));

    reemplazar(graficas,
      el('section', { class: 'tarjeta', 'aria-labelledby': 'g-reservas' },
        el('h2', { class: 'tarjeta__titulo', id: 'g-reservas', text: 'Reservas por estado' }),
        graficaBarras(aSerie(resumen.reservasPorEstado), { formato: numero, titulo: 'Reservas de tus herramientas por estado' })),
      el('section', { class: 'tarjeta', 'aria-labelledby': 'g-herramientas' },
        el('h2', { class: 'tarjeta__titulo', id: 'g-herramientas', text: 'Herramientas por estado' }),
        graficaBarras(aSerie(resumen.herramientasPorEstado), { formato: numero, titulo: 'Tus herramientas por estado', secundaria: true })));
  } catch (error) {
    reemplazar(kpis, estadoError(error, cargar));
  }
}
