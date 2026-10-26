/**
 * Calendario de disponibilidad hecho a mano. Marca los días ocupados (rangos de
 * reservas que bloquean la herramienta) y los días pasados, y permite elegir un
 * rango: primer clic = inicio, segundo clic = fin. Un rango no puede incluir días
 * ocupados ni superar la duración máxima. Cada día es un <button> con etiqueta
 * accesible, así se puede usar con teclado y lector de pantalla.
 */
import { aIso, el, fechaLocal, sumarDias } from './ui.js';

const NOMBRES_DIAS = ['Lu', 'Ma', 'Mi', 'Ju', 'Vi', 'Sá', 'Do'];
const formatoMes = new Intl.DateTimeFormat('es-CO', { month: 'long', year: 'numeric' });
const formatoDia = new Intl.DateTimeFormat('es-CO', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });

/**
 * opciones: { ocupados: [{inicio, fin}], minimo: Date, maximo: Date, maxDias,
 *             alCambiar({inicio, fin} | null), alError(mensaje) }
 */
export function crearCalendario(opciones) {
  const { minimo, maximo, maxDias = 30, alCambiar = () => {}, alError = () => {} } = opciones;
  let ocupados = new Set();
  let inicio = null;
  let fin = null;
  let mesVisible = new Date(minimo.getFullYear(), minimo.getMonth(), 1);

  const titulo = el('h3', { class: 'calendario__mes', 'aria-live': 'polite' });
  const anterior = el('button', { class: 'btn btn--fantasma btn--sm', type: 'button', 'aria-label': 'Mes anterior', text: '‹' });
  const siguiente = el('button', { class: 'btn btn--fantasma btn--sm', type: 'button', 'aria-label': 'Mes siguiente', text: '›' });
  const rejilla = el('div', { class: 'calendario__rejilla', role: 'group', 'aria-label': 'Días del mes' });
  const raiz = el('div', { class: 'calendario' },
    el('div', { class: 'calendario__cabecera' }, anterior, titulo, siguiente),
    rejilla,
    el('div', { class: 'calendario__leyenda' },
      el('span', {}, el('span', { class: 'calendario__muestra', style: 'background:#fff' }), 'Libre'),
      el('span', {}, el('span', { class: 'calendario__muestra calendario__dia--ocupado' }), 'Ocupado'),
      el('span', {}, el('span', { class: 'calendario__muestra', style: 'background:var(--color-primario)' }), 'Tu selección')));

  anterior.addEventListener('click', () => {
    mesVisible = new Date(mesVisible.getFullYear(), mesVisible.getMonth() - 1, 1);
    pintar();
  });
  siguiente.addEventListener('click', () => {
    mesVisible = new Date(mesVisible.getFullYear(), mesVisible.getMonth() + 1, 1);
    pintar();
  });

  const iso = (fecha) => aIso(fecha);
  const estaOcupado = (fecha) => ocupados.has(iso(fecha));
  const fueraDeRango = (fecha) => fecha < minimo || fecha > maximo;

  function rangoLibre(desde, hasta) {
    for (let d = new Date(desde); d <= hasta; d = sumarDias(d, 1)) {
      if (estaOcupado(d)) {
        return false;
      }
    }
    return true;
  }

  function elegir(fecha) {
    if (!inicio || fin) {
      inicio = fecha;
      fin = null;
      alCambiar(null);
    } else if (fecha < inicio) {
      inicio = fecha;
    } else {
      const dias = Math.round((fecha - inicio) / 86400000) + 1;
      if (!rangoLibre(inicio, fecha)) {
        alError('El rango elegido incluye días ocupados. Elige otras fechas.');
        inicio = fecha;
      } else if (dias > maxDias) {
        alError(`Una reserva puede durar como máximo ${maxDias} días.`);
      } else {
        fin = fecha;
        alCambiar({ inicio: iso(inicio), fin: iso(fin), dias });
      }
    }
    pintar();
  }

  function pintar() {
    titulo.textContent = formatoMes.format(mesVisible);
    anterior.disabled = mesVisible <= new Date(minimo.getFullYear(), minimo.getMonth(), 1);
    siguiente.disabled = new Date(mesVisible.getFullYear(), mesVisible.getMonth() + 1, 1) > maximo;

    rejilla.replaceChildren(...NOMBRES_DIAS.map((d) => el('div', { class: 'calendario__dia-semana', 'aria-hidden': 'true', text: d })));
    const primerDia = (mesVisible.getDay() + 6) % 7; // lunes = 0
    for (let i = 0; i < primerDia; i++) {
      rejilla.append(el('div', { class: 'calendario__vacio', 'aria-hidden': 'true' }));
    }
    const diasMes = new Date(mesVisible.getFullYear(), mesVisible.getMonth() + 1, 0).getDate();
    const hoy = iso(new Date());
    for (let dia = 1; dia <= diasMes; dia++) {
      const fecha = new Date(mesVisible.getFullYear(), mesVisible.getMonth(), dia);
      const ocupado = estaOcupado(fecha);
      const pasado = fueraDeRango(fecha);
      const seleccionado = (inicio && iso(fecha) === iso(inicio)) || (fin && iso(fecha) === iso(fin));
      const enRango = inicio && fin && fecha > inicio && fecha < fin;
      const clases = ['calendario__dia'];
      if (pasado) clases.push('calendario__dia--pasado');
      if (ocupado) clases.push('calendario__dia--ocupado');
      if (iso(fecha) === hoy) clases.push('calendario__dia--hoy');
      if (enRango) clases.push('calendario__dia--rango');
      if (seleccionado) clases.push('calendario__dia--seleccionado');
      const estado = ocupado ? ', ocupado' : pasado ? ', no disponible' : '';
      const boton = el('button', {
        class: clases.join(' '), type: 'button', text: dia,
        disabled: ocupado || pasado,
        'aria-pressed': seleccionado || enRango ? 'true' : 'false',
        'aria-label': `${formatoDia.format(fecha)}${estado}`,
      });
      boton.addEventListener('click', () => elegir(fecha));
      rejilla.append(boton);
    }
  }

  return {
    raiz,
    /** Actualiza los días ocupados ([{inicio, fin}] en ISO) y limpia la selección. */
    ponerOcupados(rangos) {
      ocupados = new Set();
      for (const rango of rangos) {
        for (let d = fechaLocal(rango.inicio); d <= fechaLocal(rango.fin); d = sumarDias(d, 1)) {
          ocupados.add(iso(d));
        }
      }
      inicio = null;
      fin = null;
      alCambiar(null);
      pintar();
    },
  };
}
