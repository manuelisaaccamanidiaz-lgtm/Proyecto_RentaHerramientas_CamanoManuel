/**
 * Campana de notificaciones de la barra (cualquier rol con sesión): badge con
 * las no leídas y un panel con las últimas, que se marcan como leídas al hacer
 * clic en ellas o con "Marcar todas como leídas".
 *
 * Actualización del conteo: al montar la barra (es decir, al cargar cada página)
 * y, como mucho, cada 60 s mientras la pestaña esté visible. Sin WebSockets ni
 * polling agresivo: los avisos no son urgentes (una solicitud o un pago se
 * atienden en minutos, no en segundos), el conteo es un COUNT indexado barato y
 * una petición por minuto y pestaña no carga al servidor. Con la pestaña oculta
 * no se consulta; al volver a ella se refresca de inmediato.
 */
import { api } from './api.js';
import { el, fechaHora, toastError } from './ui.js';

const INTERVALO_CONTEO_MS = 60_000;
const TAMANO_PANEL = 10;

function textoBadge(noLeidas) {
  return noLeidas > 9 ? '9+' : String(noLeidas);
}

/** Crea la campana y arranca la actualización del conteo. */
export function campanaNotificaciones() {
  const badge = el('span', { class: 'campana__badge', 'aria-hidden': 'true', hidden: true });
  const boton = el('button', {
    class: 'campana__boton', type: 'button', 'aria-expanded': 'false', 'aria-controls': 'panel-notificaciones',
    'aria-label': 'Notificaciones',
  }, el('span', { 'aria-hidden': 'true', text: '🔔' }), badge);

  const lista = el('ul', { class: 'campana__lista', role: 'list' });
  const marcarTodas = el('button', { class: 'campana__marcar-todas', type: 'button', text: 'Marcar todas como leídas' });
  const panel = el('div', {
    class: 'campana__panel', id: 'panel-notificaciones', role: 'region', 'aria-label': 'Notificaciones', hidden: true,
  },
  el('div', { class: 'campana__cabecera' }, el('strong', { text: 'Notificaciones' }), marcarTodas),
  lista);

  const raiz = el('div', { class: 'campana' }, boton, panel);

  function pintarConteo(noLeidas) {
    badge.hidden = noLeidas === 0;
    badge.textContent = textoBadge(noLeidas);
    boton.setAttribute('aria-label', noLeidas === 0 ? 'Notificaciones' : `Notificaciones (${noLeidas} sin leer)`);
    marcarTodas.disabled = noLeidas === 0;
  }

  async function actualizarConteo() {
    try {
      const { noLeidas } = await api.get('/notificaciones/mias/conteo');
      pintarConteo(noLeidas);
    } catch {
      // Un fallo del conteo no debe molestar con toasts cada minuto: se reintenta en el siguiente ciclo.
    }
  }

  function item(notificacion) {
    const nodo = el('li', { class: `campana__item${notificacion.leida ? '' : ' campana__item--no-leida'}` },
      el('p', { class: 'campana__mensaje', text: notificacion.mensaje }),
      el('time', { class: 'campana__fecha', datetime: notificacion.creadaEn, text: fechaHora(notificacion.creadaEn) }));
    if (!notificacion.leida) {
      nodo.tabIndex = 0;
      nodo.title = 'Marcar como leída';
      const marcar = async () => {
        try {
          await api.patch(`/notificaciones/${notificacion.id}/leida`);
          notificacion.leida = true;
          nodo.classList.remove('campana__item--no-leida');
          nodo.removeAttribute('tabindex');
          nodo.removeAttribute('title');
          actualizarConteo();
        } catch (error) {
          toastError(error, 'No se pudo marcar la notificación');
        }
      };
      nodo.addEventListener('click', marcar, { once: true });
      nodo.addEventListener('keydown', (evento) => {
        if (evento.key === 'Enter' || evento.key === ' ') {
          evento.preventDefault();
          nodo.click();
        }
      });
    }
    return nodo;
  }

  async function cargarPanel() {
    lista.replaceChildren(el('li', { class: 'campana__vacio', text: 'Cargando…' }));
    try {
      const pagina = await api.get('/notificaciones/mias', { size: TAMANO_PANEL });
      lista.replaceChildren(...(pagina.contenido.length
        ? pagina.contenido.map(item)
        : [el('li', { class: 'campana__vacio', text: 'No tienes notificaciones.' })]));
    } catch (error) {
      lista.replaceChildren(el('li', { class: 'campana__vacio', text: 'No se pudieron cargar las notificaciones.' }));
      toastError(error);
    }
  }

  function alternar(abrir) {
    panel.hidden = !abrir;
    boton.setAttribute('aria-expanded', String(abrir));
    if (abrir) {
      cargarPanel();
      actualizarConteo();
    }
  }

  boton.addEventListener('click', () => alternar(panel.hidden));
  marcarTodas.addEventListener('click', async () => {
    try {
      await api.patch('/notificaciones/mias/leidas');
      lista.querySelectorAll('.campana__item--no-leida').forEach((nodo) => {
        nodo.classList.remove('campana__item--no-leida');
        nodo.removeAttribute('tabindex');
        nodo.removeAttribute('title');
      });
      pintarConteo(0);
      await cargarPanel();
    } catch (error) {
      toastError(error, 'No se pudieron marcar las notificaciones');
    }
  });
  document.addEventListener('click', (evento) => {
    if (!panel.hidden && !raiz.contains(evento.target)) {
      alternar(false);
    }
  });
  document.addEventListener('keydown', (evento) => {
    if (evento.key === 'Escape' && !panel.hidden) {
      alternar(false);
      boton.focus();
    }
  });

  actualizarConteo();
  setInterval(() => {
    if (!document.hidden) {
      actualizarConteo();
    }
  }, INTERVALO_CONTEO_MS);
  document.addEventListener('visibilitychange', () => {
    if (!document.hidden) {
      actualizarConteo();
    }
  });

  return raiz;
}
