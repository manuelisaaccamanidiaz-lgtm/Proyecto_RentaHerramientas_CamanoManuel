/** Cliente · Mis pagos: historial con su factura descargable en PDF. */
import { api } from '../api.js';
import { requerirSesion } from '../auth.js';
import { nombreHerramienta, reservaPorId } from '../datos.js';
import { botonFactura } from '../facturas.js';
import { montarLayout } from '../layout.js';
import { crearListado } from '../listado.js';
import { badgeEstado, cop, el, fechaHora, tabla, textoMetodo } from '../ui.js';

if (requerirSesion('CLIENTE')) {
  montarLayout();
  iniciar();
}

/** Nombre de la herramienta de un pago: pago → reserva → herramienta (con caché). */
async function herramientaDelPago(pago) {
  try {
    const reserva = await reservaPorId(pago.reservaId);
    return await nombreHerramienta(reserva.herramientaId);
  } catch {
    return `Reserva #${pago.reservaId}`;
  }
}

function iniciar() {
  const listado = crearListado({
    contenedor: document.getElementById('lista'),
    cargar: (pagina) => api.get('/pagos/mios', { page: pagina, size: 10 }),
    pintar: async (respuesta) => {
      const nombres = await Promise.all(respuesta.contenido.map(herramientaDelPago));
      return tabla([
        { titulo: 'Fecha', celda: ({ pago }) => fechaHora(pago.fecha) },
        { titulo: 'Herramienta', celda: ({ nombre }) => nombre },
        { titulo: 'Referencia', celda: ({ pago }) => el('code', { text: pago.referencia }) },
        { titulo: 'Método', celda: ({ pago }) => textoMetodo(pago.metodo) },
        { titulo: 'Monto', clase: 'num', celda: ({ pago }) => cop(pago.monto) },
        { titulo: 'Estado', celda: ({ pago }) => badgeEstado(pago.estado) },
        { titulo: 'Factura', celda: ({ pago }) => botonFactura(pago.factura) },
      ], respuesta.contenido.map((pago, i) => ({ pago, nombre: nombres[i] })), { leyenda: 'Mis pagos' });
    },
    vacio: {
      titulo: 'Aún no has hecho pagos',
      texto: 'Cuando un proveedor acepte tu reserva podrás pagarla desde Mis reservas.',
      accion: { texto: 'Ir a mis reservas', href: '/cliente/reservas.html' },
    },
  });
  listado.recargar(0);
}
