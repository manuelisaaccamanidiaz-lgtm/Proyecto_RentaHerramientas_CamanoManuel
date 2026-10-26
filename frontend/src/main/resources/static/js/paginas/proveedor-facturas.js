/** Proveedor · Facturas de las reservas de sus herramientas, con descarga en PDF. */
import { api } from '../api.js';
import { requerirSesion } from '../auth.js';
import { botonFactura } from '../facturas.js';
import { montarLayout } from '../layout.js';
import { crearListado } from '../listado.js';
import { cop, el, fechaHora, tabla } from '../ui.js';

if (requerirSesion('PROVEEDOR')) {
  montarLayout();
  crearListado({
    contenedor: document.getElementById('lista'),
    cargar: (pagina) => api.get('/proveedor/facturas', { page: pagina, size: 10 }),
    pintar: (respuesta) => tabla([
      { titulo: 'Número', celda: (f) => el('strong', { text: f.numero }) },
      { titulo: 'Emitida', celda: (f) => fechaHora(f.fechaEmision) },
      { titulo: 'Concepto', celda: (f) => f.lineas[0]?.descripcion ?? '—' },
      { titulo: 'Total', clase: 'num', celda: (f) => cop(f.total) },
      { titulo: 'PDF', celda: (f) => botonFactura(f) },
    ], respuesta.contenido, { leyenda: 'Facturas de mis herramientas' }),
    vacio: {
      titulo: 'Aún no hay facturas',
      texto: 'Las facturas se emiten automáticamente cuando se aprueba el pago de una reserva.',
    },
  }).recargar(0);
}
