/** Piezas compartidas para mostrar y descargar facturas (cliente, proveedor y admin). */
import { api } from './api.js';
import { botonAccion, el, toastError } from './ui.js';

/** Descarga el PDF de una factura (lo autoriza la API: cliente, proveedor dueño o ADMIN). */
export async function descargarFactura(factura) {
  try {
    await api.descargar(`/facturas/${factura.id}/pdf`, `${factura.numero}.pdf`);
  } catch (error) {
    toastError(error, 'No se pudo descargar la factura');
  }
}

/** Botón "PDF FAC-…"; si el pago aún no tiene factura muestra un texto. */
export function botonFactura(factura) {
  if (!factura) {
    return el('span', { class: 'texto-suave texto-sm', text: 'Sin factura' });
  }
  return botonAccion(`PDF ${factura.numero}`, 'btn--secundario', () => descargarFactura(factura),
    { 'aria-label': `Descargar factura ${factura.numero} en PDF` });
}
