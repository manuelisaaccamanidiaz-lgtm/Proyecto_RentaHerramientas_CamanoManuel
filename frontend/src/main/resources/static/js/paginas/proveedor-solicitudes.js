/**
 * Proveedor · Solicitudes: todo el ciclo de una reserva de sus herramientas.
 * PENDIENTE → aceptar/rechazar · ACEPTADA → confirmar efectivo o entregar (si
 * está pagada) · ENTREGADA → registrar devolución · DEVUELTA → reportar daño.
 * Cada acción delicada pide confirmación.
 */
import { api } from '../api.js';
import { requerirSesion } from '../auth.js';
import { nombreHerramienta } from '../datos.js';
import { montarLayout } from '../layout.js';
import { crearListado, filtrosEnUrl } from '../listado.js';
import {
  abrirModal, badge, badgeEstado, botonAccion, campo, confirmar, cop, datos, el, fechaHora, modalFormulario,
  rangoFechas, tabla, textoMetodo, toast, toastError,
} from '../ui.js';

if (requerirSesion('PROVEEDOR')) {
  montarLayout();
  iniciar();
}

function iniciar() {
  const form = document.getElementById('filtros');
  const filtros = filtrosEnUrl(form);
  /** reservaId → pago más reciente (para confirmar efectivo y saber si está pagada). */
  let pagosPorReserva = new Map();

  const cargarPagos = async () => {
    const pagos = await api.get('/proveedor/pagos', { size: 50 });
    pagosPorReserva = new Map();
    for (const pago of pagos.contenido) {
      if (!pagosPorReserva.has(pago.reservaId)) {
        pagosPorReserva.set(pago.reservaId, pago);
      }
    }
  };

  const listado = crearListado({
    contenedor: document.getElementById('lista'),
    cargar: async (pagina) => {
      const valores = filtros.leer();
      filtros.guardar(valores);
      const [reservas] = await Promise.all([
        api.get('/proveedor/reservas', { ...valores, page: pagina, size: 10 }),
        cargarPagos(),
      ]);
      return reservas;
    },
    pintar: async (respuesta) => {
      const nombres = await Promise.all(respuesta.contenido.map((r) => nombreHerramienta(r.herramientaId)));
      return tabla([
        { titulo: 'Reserva', celda: ({ reserva }) => `#${reserva.id} · Cliente #${reserva.clienteId}` },
        { titulo: 'Herramienta', celda: ({ nombre }) => nombre },
        { titulo: 'Fechas', celda: ({ reserva }) => rangoFechas(reserva.fechaInicio, reserva.fechaFin) },
        { titulo: 'Total', clase: 'num', celda: ({ reserva }) => cop(reserva.precioTotal) },
        { titulo: 'Estado', celda: ({ reserva }) => badgeEstado(reserva.estado) },
        { titulo: 'Pago', celda: ({ reserva }) => estadoPago(reserva) },
        { titulo: 'Acciones', celda: ({ reserva, nombre }) => acciones(reserva, nombre) },
      ], respuesta.contenido.map((reserva, i) => ({ reserva, nombre: nombres[i] })), { leyenda: 'Solicitudes de reserva' });
    },
    vacio: {
      titulo: 'No hay solicitudes',
      texto: 'Cuando un cliente reserve una de tus herramientas aparecerá aquí.',
    },
  });

  function estadoPago(reserva) {
    if (reserva.pagada) return badge('Pagada', 'exito');
    const pago = pagosPorReserva.get(reserva.id);
    if (pago?.estado === 'PENDIENTE') return badge(`${textoMetodo(pago.metodo)} por confirmar`, 'advertencia');
    if (reserva.estado === 'ACEPTADA') return badge('Esperando pago', 'neutro');
    return el('span', { class: 'texto-suave', text: '—' });
  }

  /** Ejecuta una acción sobre la reserva, avisa y recarga la lista. */
  async function ejecutar(promesa, mensaje) {
    try {
      await promesa;
      toast(mensaje, 'exito');
      await listado.recargar();
    } catch (error) {
      toastError(error);
    }
  }

  async function conConfirmacion(opciones, accion, mensaje) {
    if (await confirmar(opciones)) {
      await ejecutar(accion(), mensaje);
    }
  }

  function acciones(reserva, nombre) {
    const lista = [botonAccion('Detalle', 'btn--fantasma', async () => verDetalle(reserva, nombre))];
    const pago = pagosPorReserva.get(reserva.id);
    const etiqueta = (texto) => ({ 'aria-label': `${texto} la reserva #${reserva.id}` });

    if (reserva.estado === 'PENDIENTE') {
      lista.push(
        botonAccion('Aceptar', 'btn--primario', () => conConfirmacion(
          { titulo: 'Aceptar reserva', mensaje: `Aceptarás la reserva de "${nombre}" del ${rangoFechas(reserva.fechaInicio, reserva.fechaFin)}. El cliente podrá pagarla.`, textoConfirmar: 'Aceptar' },
          () => api.patch(`/reservas/${reserva.id}/aceptar`), 'Reserva aceptada. El cliente ya puede pagarla.'), etiqueta('Aceptar')),
        botonAccion('Rechazar', 'btn--peligro', () => conConfirmacion(
          { titulo: 'Rechazar reserva', mensaje: 'La reserva se rechazará y las fechas quedarán libres. Esta acción no se puede deshacer.', textoConfirmar: 'Rechazar', peligro: true },
          () => api.patch(`/reservas/${reserva.id}/rechazar`), 'Reserva rechazada.'), etiqueta('Rechazar')));
    }
    if (reserva.estado === 'ACEPTADA' && pago?.estado === 'PENDIENTE') {
      lista.push(
        botonAccion('Confirmar pago', 'btn--primario', () => conConfirmacion(
          { titulo: 'Confirmar pago en efectivo', mensaje: `Confirma que recibiste ${cop(pago.monto)} en efectivo. Se emitirá la factura.`, textoConfirmar: 'Confirmar pago' },
          () => api.patch(`/pagos/${pago.id}/confirmar`), 'Pago confirmado y factura emitida.'), etiqueta('Confirmar el pago de')),
        botonAccion('Rechazar pago', 'btn--peligro', () => conConfirmacion(
          { titulo: 'Rechazar pago', mensaje: 'El pago quedará RECHAZADO y el cliente deberá pagar de nuevo.', textoConfirmar: 'Rechazar pago', peligro: true },
          () => api.patch(`/pagos/${pago.id}/rechazar`), 'Pago rechazado.'), etiqueta('Rechazar el pago de')));
    }
    if (reserva.estado === 'ACEPTADA' && reserva.pagada) {
      lista.push(botonAccion('Entregar', 'btn--primario', () => conConfirmacion(
        { titulo: 'Registrar entrega', mensaje: `Confirma que entregaste "${nombre}" al cliente.`, textoConfirmar: 'Registrar entrega' },
        () => api.patch(`/reservas/${reserva.id}/entregar`), 'Entrega registrada.'), etiqueta('Entregar')));
    }
    if (reserva.estado === 'ENTREGADA') {
      lista.push(botonAccion('Recibir devolución', 'btn--primario', () => conConfirmacion(
        { titulo: 'Registrar devolución', mensaje: `Confirma que el cliente te devolvió "${nombre}".`, textoConfirmar: 'Registrar devolución' },
        () => api.patch(`/reservas/${reserva.id}/devolver`), 'Devolución registrada.'), etiqueta('Registrar la devolución de')));
    }
    if (reserva.estado === 'DEVUELTA') {
      lista.push(botonAccion('Reportar daño', 'btn--peligro', () => reportarDanio(reserva, nombre), etiqueta('Reportar un daño en')));
    }
    return el('div', { class: 'acciones' }, lista);
  }

  async function reportarDanio(reserva, nombre) {
    const resultado = await modalFormulario({
      titulo: 'Reportar daño',
      textoEnviar: 'Reportar daño',
      peligro: true,
      campos: [
        el('p', { class: 'aviso aviso--advertencia', text: `Al reportar el daño, "${nombre}" quedará marcada como DAÑADA y no se podrá reservar hasta que la repares.` }),
        campo({ etiqueta: 'Descripción del daño', nombre: 'descripcion', tipo: 'textarea', requerido: true, atributos: { maxlength: 2000 } }),
        campo({ etiqueta: 'Costo estimado (COP)', nombre: 'costoEstimado', tipo: 'number', ayuda: 'Opcional. Déjalo vacío si aún no lo cotizas.', atributos: { min: 0, step: 1, inputmode: 'numeric' } }),
      ],
      alEnviar: (valores) => api.post(`/reservas/${reserva.id}/danios`, {
        descripcion: valores.descripcion,
        costoEstimado: valores.costoEstimado === null ? null : Number(valores.costoEstimado),
      }),
    });
    if (resultado) {
      toast('Daño reportado. La herramienta quedó marcada como dañada.', 'exito');
      await listado.recargar();
    }
  }

  function verDetalle(reserva, nombre) {
    const pago = pagosPorReserva.get(reserva.id);
    abrirModal({
      titulo: `Reserva #${reserva.id}`,
      ancho: 640,
      contenido: el('div', { class: 'pila' },
        datos([
          ['Herramienta', nombre],
          ['Cliente', `#${reserva.clienteId}`],
          ['Estado', badgeEstado(reserva.estado)],
          ['Fechas', `${rangoFechas(reserva.fechaInicio, reserva.fechaFin)} (${reserva.dias} días)`],
          ['Total', cop(reserva.precioTotal)],
          ['Pago', pago ? `${textoMetodo(pago.metodo)} · ${pago.estado.toLowerCase()} · ${pago.referencia}` : 'Sin pagos'],
          ['Entregada', fechaHora(reserva.fechaEntrega)],
          ['Devuelta', fechaHora(reserva.fechaDevolucion)],
        ]),
        reserva.reportesDanio.length
          ? el('section', { class: 'pila' }, el('h3', { text: 'Reportes de daño' }),
            el('ul', { class: 'lista-simple' }, reserva.reportesDanio.map((r) => el('li', {},
              el('strong', { text: r.descripcion }),
              el('span', { class: 'texto-suave', text: ` · ${fechaHora(r.reportadoEn)}${r.costoEstimado != null ? ` · ${cop(r.costoEstimado)}` : ''}` })))))
          : null),
      acciones: [{ texto: 'Cerrar', clase: 'btn--secundario' }],
    });
  }

  form.addEventListener('change', () => listado.reiniciar());
  form.addEventListener('submit', (evento) => {
    evento.preventDefault();
    listado.reiniciar();
  });
  listado.recargar(0);
}
