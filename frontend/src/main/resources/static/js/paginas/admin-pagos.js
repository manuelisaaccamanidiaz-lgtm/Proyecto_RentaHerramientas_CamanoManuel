/** Admin · Pagos y facturas: historial de pagos con filtros (y resolución de efectivo pendiente) y todas las facturas. */
import { api } from '../api.js';
import { requerirSesion } from '../auth.js';
import { botonFactura } from '../facturas.js';
import { montarLayout } from '../layout.js';
import { crearListado, filtrosEnUrl } from '../listado.js';
import {
  badgeEstado, botonAccion, confirmar, cop, el, fechaHora, tabla, textoMetodo, toast, toastError,
} from '../ui.js';

if (requerirSesion('ADMIN')) {
  montarLayout();
  iniciar();
}

function iniciar() {
  const form = document.getElementById('filtros');
  const filtros = filtrosEnUrl(form);

  const pagos = crearListado({
    contenedor: document.getElementById('lista-pagos'),
    cargar: (pagina) => {
      const valores = filtros.leer();
      filtros.guardar(valores);
      return api.get('/admin/pagos', { ...valores, page: pagina, size: 10 });
    },
    pintar: (respuesta) => tabla([
      { titulo: 'Fecha', celda: (p) => fechaHora(p.fecha) },
      { titulo: 'Referencia', celda: (p) => el('code', { text: p.referencia }) },
      { titulo: 'Reserva', celda: (p) => `#${p.reservaId}` },
      { titulo: 'Método', celda: (p) => textoMetodo(p.metodo) },
      { titulo: 'Monto', clase: 'num', celda: (p) => cop(p.monto) },
      { titulo: 'Estado', celda: (p) => badgeEstado(p.estado) },
      { titulo: 'Factura / acciones', celda: (p) => (p.estado === 'PENDIENTE' ? accionesPendiente(p) : botonFactura(p.factura)) },
    ], respuesta.contenido, { leyenda: 'Pagos' }),
    vacio: { titulo: 'No hay pagos con esos filtros', texto: 'Cambia el estado, el método o el rango de fechas.' },
  });

  const facturas = crearListado({
    contenedor: document.getElementById('lista-facturas'),
    cargar: (pagina) => api.get('/admin/facturas', { page: pagina, size: 10 }),
    pintar: (respuesta) => tabla([
      { titulo: 'Número', celda: (f) => el('strong', { text: f.numero }) },
      { titulo: 'Emitida', celda: (f) => fechaHora(f.fechaEmision) },
      { titulo: 'Concepto', celda: (f) => f.lineas[0]?.descripcion ?? '—' },
      { titulo: 'Pago', celda: (f) => `#${f.pagoId}` },
      { titulo: 'Total', clase: 'num', celda: (f) => cop(f.total) },
      { titulo: 'PDF', celda: (f) => botonFactura(f) },
    ], respuesta.contenido, { leyenda: 'Facturas' }),
    vacio: { titulo: 'Aún no hay facturas', texto: 'Se emiten al aprobarse cada pago.' },
  });

  function accionesPendiente(pago) {
    const resolver = async (accion) => {
      const confirmarPago = accion === 'confirmar';
      const ok = await confirmar(confirmarPago
        ? { titulo: 'Confirmar pago', mensaje: `Se aprobará el pago ${pago.referencia} por ${cop(pago.monto)} y se emitirá su factura.`, textoConfirmar: 'Confirmar pago' }
        : { titulo: 'Rechazar pago', mensaje: `El pago ${pago.referencia} quedará RECHAZADO.`, textoConfirmar: 'Rechazar pago', peligro: true });
      if (!ok) return;
      try {
        await api.patch(`/pagos/${pago.id}/${accion}`);
        toast(confirmarPago ? 'Pago confirmado y factura emitida.' : 'Pago rechazado.', 'exito');
        await Promise.all([pagos.recargar(), facturas.recargar()]);
      } catch (error) {
        toastError(error);
      }
    };
    return el('div', { class: 'acciones' },
      botonAccion('Confirmar', 'btn--primario', () => resolver('confirmar'), { 'aria-label': `Confirmar el pago ${pago.referencia}` }),
      botonAccion('Rechazar', 'btn--peligro', () => resolver('rechazar'), { 'aria-label': `Rechazar el pago ${pago.referencia}` }));
  }

  // Pestañas Pagos / Facturas (patrón ARIA tabs).
  const pestanas = [...document.querySelectorAll('[role="tab"]')];
  const activar = (pestana) => {
    for (const otra of pestanas) {
      const activa = otra === pestana;
      otra.setAttribute('aria-selected', String(activa));
      otra.tabIndex = activa ? 0 : -1;
      document.getElementById(otra.getAttribute('aria-controls')).hidden = !activa;
    }
    pestana.focus();
  };
  pestanas.forEach((pestana, i) => {
    pestana.addEventListener('click', () => activar(pestana));
    pestana.addEventListener('keydown', (evento) => {
      if (evento.key === 'ArrowRight' || evento.key === 'ArrowLeft') {
        const siguiente = pestanas[(i + (evento.key === 'ArrowRight' ? 1 : pestanas.length - 1)) % pestanas.length];
        activar(siguiente);
      }
    });
  });

  form.addEventListener('change', () => pagos.reiniciar());
  form.addEventListener('submit', (evento) => {
    evento.preventDefault();
    pagos.reiniciar();
  });
  pagos.recargar(0);
  facturas.recargar(0);
}
