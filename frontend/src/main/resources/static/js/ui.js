/**
 * Utilidades de interfaz compartidas por todas las páginas.
 *
 * SEGURIDAD (XSS): los datos del servidor NUNCA se insertan con innerHTML. Todo
 * se construye con el helper el(), que usa textContent y atributos, o se
 * escapa con escapeHtml(). Lo único que se inserta como marcado son las
 * ilustraciones SVG constantes definidas en este mismo archivo.
 */
import { ApiError } from './api.js';

// =================================================================== DOM

/**
 * Crea un elemento. props admite: class, text, html NO (a propósito),
 * dataset {}, on {evento: fn}, y cualquier otro atributo (aria-*, type, href...).
 * Los hijos pueden ser nodos, textos (se insertan como texto) o null/false (se ignoran).
 */
export function el(etiqueta, props = {}, ...hijos) {
  const nodo = document.createElement(etiqueta);
  for (const [clave, valor] of Object.entries(props)) {
    if (valor === undefined || valor === null || valor === false) {
      continue;
    }
    if (clave === 'class') {
      nodo.className = valor;
    } else if (clave === 'text') {
      nodo.textContent = String(valor);
    } else if (clave === 'dataset') {
      Object.assign(nodo.dataset, valor);
    } else if (clave === 'on') {
      for (const [evento, manejador] of Object.entries(valor)) {
        nodo.addEventListener(evento, manejador);
      }
    } else if (valor === true) {
      nodo.setAttribute(clave, '');
    } else {
      nodo.setAttribute(clave, String(valor));
    }
  }
  agregar(nodo, hijos);
  return nodo;
}

function agregar(nodo, hijos) {
  for (const hijo of hijos.flat(Infinity)) {
    if (hijo === null || hijo === undefined || hijo === false) {
      continue;
    }
    nodo.append(hijo instanceof Node ? hijo : document.createTextNode(String(hijo)));
  }
}

/** Reemplaza el contenido de un contenedor por los hijos dados. */
export function reemplazar(contenedor, ...hijos) {
  contenedor.replaceChildren();
  agregar(contenedor, hijos);
}

/** Escapa texto para los raros casos en que haga falta construir HTML a mano. */
export function escapeHtml(texto) {
  return String(texto ?? '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');
}

/** Crea un SVG a partir de un marcado CONSTANTE de esta aplicación (nunca datos del servidor). */
function svgConstante(marcado) {
  const plantilla = document.createElement('template');
  plantilla.innerHTML = marcado.trim();
  return plantilla.content.firstElementChild;
}

export const $ = (selector, raiz = document) => raiz.querySelector(selector);
export const $$ = (selector, raiz = document) => [...raiz.querySelectorAll(selector)];

/** Parámetros de la URL actual como objeto plano. */
export function parametrosUrl() {
  return Object.fromEntries(new URLSearchParams(location.search));
}

// =================================================================== formato

const formatoCop = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  minimumFractionDigits: 0,
  maximumFractionDigits: 0,
});
const formatoCopDecimales = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});
const formatoNumero = new Intl.NumberFormat('es-CO');
const formatoFecha = new Intl.DateTimeFormat('es-CO', { day: 'numeric', month: 'short', year: 'numeric' });
const formatoFechaLarga = new Intl.DateTimeFormat('es-CO', { day: 'numeric', month: 'long', year: 'numeric' });
const formatoFechaHora = new Intl.DateTimeFormat('es-CO', {
  day: 'numeric', month: 'short', year: 'numeric', hour: 'numeric', minute: '2-digit',
});

/** Pesos colombianos: "$ 75.000" (con centavos solo si los hay). */
export function cop(valor) {
  const numero = Number(valor ?? 0);
  return Number.isInteger(numero) ? formatoCop.format(numero) : formatoCopDecimales.format(numero);
}

export function numero(valor) {
  return formatoNumero.format(Number(valor ?? 0));
}

/** "2026-10-15" → fecha local (sin corrimiento de zona horaria). */
export function fechaLocal(iso) {
  const [anio, mes, dia] = String(iso).slice(0, 10).split('-').map(Number);
  return new Date(anio, mes - 1, dia);
}

/** Fecha local → "2026-10-15". */
export function aIso(fecha) {
  const mes = String(fecha.getMonth() + 1).padStart(2, '0');
  const dia = String(fecha.getDate()).padStart(2, '0');
  return `${fecha.getFullYear()}-${mes}-${dia}`;
}

export function hoyIso() {
  return aIso(new Date());
}

export function sumarDias(fecha, dias) {
  const copia = new Date(fecha);
  copia.setDate(copia.getDate() + dias);
  return copia;
}

export function fecha(iso) {
  return iso ? formatoFecha.format(fechaLocal(iso)) : '—';
}

export function fechaLarga(iso) {
  return iso ? formatoFechaLarga.format(fechaLocal(iso)) : '—';
}

export function fechaHora(iso) {
  return iso ? formatoFechaHora.format(new Date(iso)) : '—';
}

export function rangoFechas(inicio, fin) {
  return inicio === fin ? fecha(inicio) : `${fecha(inicio)} – ${fecha(fin)}`;
}

// =================================================================== estados y badges

/** Texto y color de cada estado de reserva, pago, herramienta y usuario. */
const ESTADOS = {
  // reserva
  PENDIENTE: ['Pendiente', 'advertencia'],
  ACEPTADA: ['Aceptada', 'info'],
  RECHAZADA: ['Rechazada', 'peligro'],
  ENTREGADA: ['Entregada', 'primario'],
  DEVUELTA: ['Devuelta', 'exito'],
  VENCIDA: ['Vencida', 'neutro'],
  CANCELADA: ['Cancelada', 'neutro'],
  // pago
  APROBADO: ['Aprobado', 'exito'],
  RECHAZADO: ['Rechazado', 'peligro'],
  // herramienta
  DISPONIBLE: ['Disponible', 'exito'],
  EN_MANTENIMIENTO: ['En mantenimiento', 'advertencia'],
  DANADA: ['Dañada', 'peligro'],
  INACTIVA: ['Inactiva', 'neutro'],
  // roles
  ADMIN: ['Administrador', 'secundario'],
  PROVEEDOR: ['Proveedor', 'primario'],
  CLIENTE: ['Cliente', 'info'],
};

const METODOS = { TARJETA: 'Tarjeta', PSE: 'PSE', EFECTIVO: 'Efectivo' };

export function textoEstado(estado) {
  return ESTADOS[estado]?.[0] ?? String(estado ?? '—');
}

export function textoMetodo(metodo) {
  return METODOS[metodo] ?? String(metodo ?? '—');
}

export function badge(texto, tono = 'neutro') {
  return el('span', { class: `badge badge--${tono}`, text: texto });
}

export function badgeEstado(estado) {
  const [texto, tono] = ESTADOS[estado] ?? [String(estado ?? '—'), 'neutro'];
  return badge(texto, tono === 'secundario' ? 'neutro' : tono);
}

// =================================================================== toasts

function contenedorToasts() {
  let contenedor = document.getElementById('toasts');
  if (!contenedor) {
    contenedor = el('div', { id: 'toasts', class: 'toasts', 'aria-live': 'polite', 'aria-atomic': 'false' });
    document.body.append(contenedor);
  }
  return contenedor;
}

/** Notificación temporal. tipo: info | exito | peligro | advertencia. */
export function toast(mensaje, tipo = 'info', duracion = 5000) {
  const nodo = el('div', { class: `toast toast--${tipo}`, role: tipo === 'peligro' ? 'alert' : 'status' },
    el('span', { class: 'toast__mensaje', text: mensaje }),
    el('button', { class: 'toast__cerrar', type: 'button', 'aria-label': 'Cerrar notificación', text: '×' }));
  const cerrar = () => {
    nodo.classList.add('saliendo');
    setTimeout(() => nodo.remove(), 200);
  };
  nodo.querySelector('button').addEventListener('click', cerrar);
  contenedorToasts().append(nodo);
  setTimeout(cerrar, duracion);
}

const CLAVE_AVISO = 'rentaherramientas.aviso';

/** Guarda un toast para mostrarlo en la próxima página (tras una redirección). */
export function toastEnSiguientePagina(mensaje, tipo = 'info') {
  try {
    sessionStorage.setItem(CLAVE_AVISO, JSON.stringify({ mensaje, tipo }));
  } catch {
    // sin almacenamiento: el aviso simplemente no se muestra
  }
}

/** Muestra el toast pendiente de la página anterior, si lo hay. */
export function mostrarToastPendiente() {
  try {
    const guardado = sessionStorage.getItem(CLAVE_AVISO);
    if (guardado) {
      sessionStorage.removeItem(CLAVE_AVISO);
      const { mensaje, tipo } = JSON.parse(guardado);
      toast(String(mensaje), tipo);
    }
  } catch {
    // aviso corrupto o sin almacenamiento: se ignora
  }
}

/** Muestra el mensaje adecuado para cualquier error (ApiError o inesperado). */
export function toastError(error, prefijo) {
  const mensaje = error instanceof ApiError ? error.detalle : 'Ocurrió un error inesperado.';
  toast(prefijo ? `${prefijo}: ${mensaje}` : mensaje, 'peligro', 7000);
}

// =================================================================== modales

/**
 * Abre un <dialog> modal. contenido: nodo o lista de nodos. acciones: botones del
 * pie [{ texto, clase, valor }]; al pulsarlos el modal se cierra y la promesa
 * se resuelve con su valor (null si se cierra con Escape o la X).
 */
export function abrirModal({ titulo, contenido, acciones = [], ancho }) {
  const idTitulo = `modal-titulo-${crypto.randomUUID()}`;
  const dialogo = el('dialog', { class: 'modal', 'aria-labelledby': idTitulo });
  if (ancho) {
    dialogo.style.width = `min(${ancho}px, calc(100vw - 2rem))`;
  }
  const botonCerrar = el('button', { class: 'modal__cerrar', type: 'button', 'aria-label': 'Cerrar', text: '×' });
  const pie = el('div', { class: 'modal__pie' });
  dialogo.append(
    el('div', { class: 'modal__cabecera' }, el('h2', { class: 'modal__titulo', id: idTitulo, text: titulo }), botonCerrar),
    el('div', { class: 'modal__cuerpo' }, contenido),
    pie);

  const resultado = new Promise((resolver) => {
    let valor = null;
    for (const accion of acciones) {
      const boton = el('button', { class: `btn ${accion.clase ?? ''}`, type: 'button', text: accion.texto });
      boton.addEventListener('click', () => {
        valor = accion.valor ?? null;
        dialogo.close();
      });
      pie.append(boton);
    }
    botonCerrar.addEventListener('click', () => dialogo.close());
    dialogo.addEventListener('close', () => {
      dialogo.remove();
      resolver(valor);
    });
  });

  document.body.append(dialogo);
  dialogo.showModal();
  return { dialogo, pie, resultado, cerrar: () => dialogo.close() };
}

/** Confirmación para acciones delicadas. Devuelve true solo si se confirma. */
export async function confirmar({ titulo, mensaje, textoConfirmar = 'Confirmar', peligro = false }) {
  const { resultado } = abrirModal({
    titulo,
    contenido: el('p', { text: mensaje }),
    acciones: [
      { texto: 'Cancelar', clase: 'btn--fantasma', valor: false },
      { texto: textoConfirmar, clase: peligro ? 'btn--peligro' : 'btn--primario', valor: true },
    ],
  });
  return (await resultado) === true;
}

/**
 * Modal con formulario. alEnviar(datos, form) se ejecuta al enviar; si lanza un
 * ApiError, los errores se muestran en el formulario y el modal sigue abierto.
 * Resuelve con lo que devuelva alEnviar, o null si se cancela.
 */
export function modalFormulario({ titulo, campos, textoEnviar = 'Guardar', alEnviar, ancho, peligro = false }) {
  const form = el('form', { class: 'formulario', novalidate: true },
    el('div', { class: 'aviso aviso--peligro', role: 'alert', 'data-aviso': '' }),
    campos);
  const botonEnviar = el('button', { class: `btn ${peligro ? 'btn--peligro' : 'btn--primario'}`, type: 'submit' },
    el('span', { text: textoEnviar }));
  const idForm = `form-${crypto.randomUUID()}`;
  form.id = idForm;
  botonEnviar.setAttribute('form', idForm);

  const modal = abrirModal({ titulo, contenido: form, ancho });
  const cancelar = el('button', { class: 'btn btn--fantasma', type: 'button', text: 'Cancelar' });
  cancelar.addEventListener('click', () => modal.cerrar());
  modal.pie.append(cancelar, botonEnviar);

  let valor = null;
  form.addEventListener('submit', async (evento) => {
    evento.preventDefault();
    limpiarErrores(form);
    if (!validarFormulario(form)) {
      return;
    }
    await conCarga(botonEnviar, async () => {
      try {
        valor = await alEnviar(datosFormulario(form), form);
        modal.cerrar();
      } catch (error) {
        mostrarErrores(form, error);
      }
    });
  });
  // El primer campo recibe el foco al abrir.
  queueMicrotask(() => form.querySelector('input:not([type="hidden"]), select, textarea')?.focus());
  return modal.resultado.then(() => valor);
}

// =================================================================== formularios

/** Campo con etiqueta, ayuda opcional y hueco para su mensaje de error. */
export function campo({ etiqueta, nombre, tipo = 'text', valor, requerido = false, ayuda, opciones, atributos = {} }) {
  const id = `campo-${nombre.replaceAll('.', '-')}-${crypto.randomUUID().slice(0, 8)}`;
  const idError = `${id}-error`;
  const idAyuda = ayuda ? `${id}-ayuda` : null;
  const describe = [idAyuda, idError].filter(Boolean).join(' ');
  let control;
  if (tipo === 'select') {
    control = el('select', { id, name: nombre, required: requerido, 'aria-describedby': describe, ...atributos },
      (opciones ?? []).map(([v, t]) => el('option', { value: v, text: t, selected: String(v) === String(valor ?? '') })));
  } else if (tipo === 'textarea') {
    control = el('textarea', { id, name: nombre, required: requerido, 'aria-describedby': describe, ...atributos });
    control.value = valor ?? '';
  } else {
    control = el('input', { id, name: nombre, type: tipo, required: requerido, 'aria-describedby': describe, ...atributos });
    if (valor !== undefined && valor !== null) {
      control.value = valor;
    }
  }
  return el('div', { class: 'campo' },
    el('label', { for: id }, etiqueta, requerido ? el('span', { class: 'requerido', 'aria-hidden': 'true', text: ' *' }) : null),
    control,
    ayuda ? el('p', { class: 'campo__ayuda', id: idAyuda, text: ayuda }) : null,
    el('p', { class: 'campo__error', id: idError }));
}

/**
 * Lee un formulario como objeto. Los nombres con punto ("datosCliente.documento")
 * se convierten en objetos anidados; los campos vacíos quedan en null y los
 * deshabilitados (fieldset disabled) se omiten.
 */
export function datosFormulario(form) {
  const datos = {};
  for (const control of form.elements) {
    // :disabled incluye los controles dentro de un <fieldset disabled> (la propiedad .disabled no).
    if (!control.name || control.matches(':disabled') || control.type === 'submit' || control.type === 'button') {
      continue;
    }
    if ((control.type === 'radio' || control.type === 'checkbox') && !control.checked) {
      if (control.type === 'checkbox' && control.dataset.lista !== undefined) {
        asignar(datos, control.name, [], true);
      }
      continue;
    }
    let valor = control.value.trim();
    if (control.type === 'checkbox' && control.dataset.lista !== undefined) {
      asignar(datos, control.name, valor, true);
      continue;
    }
    if (control.type === 'file') {
      continue;
    }
    asignar(datos, control.name, valor === '' ? null : valor, false);
  }
  return datos;
}

function asignar(objeto, ruta, valor, esLista) {
  const partes = ruta.split('.');
  let actual = objeto;
  for (const parte of partes.slice(0, -1)) {
    actual[parte] ??= {};
    actual = actual[parte];
  }
  const clave = partes.at(-1);
  if (esLista) {
    actual[clave] ??= [];
    if (valor !== null && !Array.isArray(valor)) {
      actual[clave].push(valor);
    }
  } else {
    actual[clave] = valor;
  }
}

/** Validación nativa del navegador, mostrada con nuestros mensajes por campo. */
export function validarFormulario(form) {
  let valido = true;
  for (const control of form.elements) {
    if (!control.willValidate || control.checkValidity()) {
      continue;
    }
    valido = false;
    marcarError(form, control.name, mensajeValidacion(control));
  }
  if (!valido) {
    form.querySelector('[aria-invalid="true"]')?.focus();
  }
  return valido;
}

function mensajeValidacion(control) {
  const v = control.validity;
  if (v.valueMissing) return 'Este campo es obligatorio.';
  if (v.typeMismatch && control.type === 'email') return 'Escribe un correo válido (ej.: nombre@correo.com).';
  if (v.tooShort) return `Debe tener al menos ${control.minLength} caracteres.`;
  if (v.tooLong) return `Debe tener como máximo ${control.maxLength} caracteres.`;
  if (v.rangeUnderflow) return `El valor mínimo es ${control.min}.`;
  if (v.rangeOverflow) return `El valor máximo es ${control.max}.`;
  if (v.patternMismatch) return control.dataset.mensajePatron || 'El formato no es válido.';
  return control.validationMessage || 'Revisa este campo.';
}

function marcarError(form, nombre, mensaje) {
  const control = form.querySelector(`[name="${CSS.escape(nombre)}"]`);
  if (!control) {
    return false;
  }
  control.setAttribute('aria-invalid', 'true');
  const idError = (control.getAttribute('aria-describedby') || '').split(' ').find((id) => id.endsWith('-error'));
  const nodoError = idError ? document.getElementById(idError) : null;
  if (nodoError) {
    nodoError.textContent = mensaje;
  }
  return true;
}

export function limpiarErrores(form) {
  for (const control of form.querySelectorAll('[aria-invalid="true"]')) {
    control.removeAttribute('aria-invalid');
  }
  for (const nodo of form.querySelectorAll('.campo__error')) {
    nodo.textContent = '';
  }
  const aviso = form.querySelector('[data-aviso]');
  if (aviso) {
    aviso.textContent = '';
  }
}

/**
 * Muestra un error de la API en el formulario: cada entrada de "errores" va a su
 * campo, y el detalle general al aviso del formulario.
 */
export function mostrarErrores(form, error) {
  const aviso = form.querySelector('[data-aviso]');
  if (!(error instanceof ApiError)) {
    if (aviso) aviso.textContent = 'Ocurrió un error inesperado. Inténtalo de nuevo.';
    return;
  }
  let algunoEnCampo = false;
  for (const [nombre, mensaje] of Object.entries(error.errores)) {
    algunoEnCampo = marcarError(form, nombre, mensaje) || algunoEnCampo;
  }
  if (aviso) {
    aviso.textContent = algunoEnCampo ? 'Revisa los campos marcados.' : error.detalle;
  }
  form.querySelector('[aria-invalid="true"]')?.focus();
}

/** Pone un botón en estado "cargando" mientras dura la acción. */
export async function conCarga(boton, accion) {
  boton.setAttribute('aria-busy', 'true');
  boton.disabled = true;
  try {
    return await accion();
  } finally {
    boton.removeAttribute('aria-busy');
    boton.disabled = false;
  }
}

const ICONO_CARGA = `
<svg class="zona-carga__icono" viewBox="0 0 24 24" aria-hidden="true" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
  <rect x="3" y="3" width="18" height="18" rx="3"/>
  <circle cx="9" cy="9" r="2"/>
  <path d="m21 15-4.5-4.5L8 19"/>
</svg>`;

const formatoPeso = new Intl.NumberFormat('es-CO', { maximumFractionDigits: 1 });

/** 245760 → "240 KB"; 1258291 → "1,2 MB". */
export function pesoArchivo(bytes) {
  return bytes < 1024 * 1024
    ? `${formatoPeso.format(Math.max(1, Math.round(bytes / 1024)))} KB`
    : `${formatoPeso.format(bytes / (1024 * 1024))} MB`;
}

/**
 * Convierte un <input type="file"> en una zona de carga con arrastrar y soltar.
 * El input real queda encima de la zona, transparente: el clic, el teclado y
 * soltar un archivo los resuelve el navegador, y su "change" llega intacto a
 * quien lo escuche. Al elegir un archivo muestra miniatura, nombre, peso y un
 * botón para quitarlo (vacía el input y vuelve a disparar "change").
 */
export function zonaCarga(input, { texto = 'Arrastra una imagen o haz clic para elegirla' } = {}) {
  input.classList.add('zona-carga__input');
  const archivo = el('div', { class: 'zona-carga__archivo', 'aria-live': 'polite' });
  const zona = el('div', { class: 'zona-carga' },
    input,
    el('div', { class: 'zona-carga__contenido', 'aria-hidden': 'true' },
      svgConstante(ICONO_CARGA),
      el('span', { class: 'zona-carga__texto', text: texto })));

  const resaltar = (activo) => zona.classList.toggle('zona-carga--arrastrando', activo);
  input.addEventListener('dragenter', () => resaltar(true));
  input.addEventListener('dragleave', () => resaltar(false));
  input.addEventListener('drop', () => resaltar(false));

  input.addEventListener('change', () => {
    const elegido = input.files[0];
    if (!elegido) {
      archivo.replaceChildren();
      return;
    }
    let miniatura;
    if (elegido.type.startsWith('image/')) {
      miniatura = el('img', { class: 'zona-carga__miniatura', alt: '', src: URL.createObjectURL(elegido) });
      const liberar = () => URL.revokeObjectURL(miniatura.src);
      miniatura.addEventListener('load', liberar, { once: true });
      miniatura.addEventListener('error', liberar, { once: true });
    } else {
      miniatura = el('span', { class: 'zona-carga__miniatura', 'aria-hidden': 'true' }, svgConstante(ICONO_CARGA));
    }
    const quitar = el('button', { class: 'btn btn--fantasma btn--sm', type: 'button', 'aria-label': `Quitar ${elegido.name}`, text: 'Quitar' });
    quitar.addEventListener('click', () => {
      input.value = '';
      input.dispatchEvent(new Event('change', { bubbles: true }));
      input.focus();
    });
    reemplazar(archivo, miniatura,
      el('div', { class: 'zona-carga__datos' },
        el('span', { class: 'zona-carga__nombre', title: elegido.name, text: elegido.name }),
        el('span', { class: 'zona-carga__peso', text: pesoArchivo(elegido.size) })),
      quitar);
  });

  return el('div', { class: 'zona-carga__grupo' }, zona, archivo);
}

/** Botón de acción para tablas que ejecuta una función con estado de carga. */
export function botonAccion(texto, clase, accion, atributos = {}) {
  const boton = el('button', { class: `btn btn--sm ${clase}`, type: 'button', ...atributos }, el('span', { text: texto }));
  boton.addEventListener('click', () => conCarga(boton, accion));
  return boton;
}

// =================================================================== carga, vacío y error

const ILUSTRACION_VACIO = `
<svg class="vacio__ilustracion" viewBox="0 0 120 120" aria-hidden="true">
  <circle cx="60" cy="60" r="56" fill="#FFF7ED"/>
  <rect x="26" y="50" width="68" height="40" rx="6" fill="#F97316"/>
  <rect x="26" y="50" width="68" height="10" rx="4" fill="#EA580C"/>
  <path d="M46 50v-8a6 6 0 0 1 6-6h16a6 6 0 0 1 6 6v8" fill="none" stroke="#1F2937" stroke-width="5"/>
  <rect x="54" y="62" width="12" height="10" rx="2" fill="#1F2937"/>
  <path d="M20 96h80" stroke="#E2E8F0" stroke-width="4" stroke-linecap="round"/>
</svg>`;

const ILUSTRACION_ERROR = `
<svg class="vacio__ilustracion" viewBox="0 0 120 120" aria-hidden="true">
  <circle cx="60" cy="60" r="56" fill="#FEE2E2"/>
  <path d="M60 28 96 90H24Z" fill="#DC2626"/>
  <rect x="56" y="48" width="8" height="24" rx="3" fill="#fff"/>
  <circle cx="60" cy="80" r="4.5" fill="#fff"/>
</svg>`;

/** Estado vacío ilustrado con una acción opcional { texto, href }. */
export function estadoVacio({ titulo, texto, accion }) {
  return el('div', { class: 'vacio' },
    svgConstante(ILUSTRACION_VACIO),
    el('h2', { class: 'vacio__titulo', text: titulo }),
    texto ? el('p', { class: 'vacio__texto', text: texto }) : null,
    accion ? el('a', { class: 'btn btn--primario', href: accion.href, text: accion.texto }) : null);
}

/** Estado de error de carga con botón para reintentar. */
export function estadoError(error, reintentar) {
  const boton = reintentar ? el('button', { class: 'btn btn--secundario', type: 'button', text: 'Reintentar' }) : null;
  boton?.addEventListener('click', reintentar);
  return el('div', { class: 'vacio vacio--error', role: 'alert' },
    svgConstante(ILUSTRACION_ERROR),
    el('h2', { class: 'vacio__titulo', text: 'No pudimos cargar la información' }),
    el('p', { class: 'vacio__texto', text: error instanceof ApiError ? error.detalle : 'Ocurrió un error inesperado.' }),
    boton);
}

/** Placeholders animados mientras llegan los datos. tipo: filas | tarjetas | kpi */
export function esqueleto(tipo = 'filas', cantidad = 5) {
  const contenedor = el('div', { 'aria-busy': 'true', 'aria-label': 'Cargando…', role: 'status' });
  if (tipo === 'tarjetas') {
    contenedor.className = 'rejilla rejilla-4';
    for (let i = 0; i < cantidad; i++) {
      contenedor.append(el('div', { class: 'tarjeta herramienta' },
        el('div', { class: 'skeleton skeleton--imagen' }),
        el('div', { class: 'herramienta__cuerpo' },
          el('div', { class: 'skeleton skeleton--titulo' }),
          el('div', { class: 'skeleton skeleton--linea' }),
          el('div', { class: 'skeleton skeleton--linea', style: 'width:40%' }))));
    }
  } else if (tipo === 'kpi') {
    contenedor.className = 'rejilla rejilla-4';
    for (let i = 0; i < cantidad; i++) {
      contenedor.append(el('div', { class: 'skeleton skeleton--kpi' }));
    }
  } else {
    for (let i = 0; i < cantidad; i++) {
      contenedor.append(el('div', { class: 'skeleton skeleton--fila' }));
    }
  }
  return contenedor;
}

// =================================================================== paginación

/** Controles de paginación para una PaginaResponse del backend (página base 0). */
export function paginacion(pagina, alCambiar) {
  if (!pagina || pagina.totalPaginas <= 1) {
    return pagina && pagina.totalElementos > 0
      ? el('div', { class: 'paginacion' },
        el('p', { class: 'paginacion__info', text: `${numero(pagina.totalElementos)} resultado(s)` }))
      : el('div');
  }
  const actual = pagina.pagina;
  const total = pagina.totalPaginas;
  const boton = (texto, destino, etiqueta, esActual = false) => {
    const b = el('button', {
      class: 'btn btn--sm', type: 'button', text: texto,
      'aria-label': etiqueta, 'aria-current': esActual ? 'page' : null,
      disabled: destino < 0 || destino >= total || (esActual ? true : null),
    });
    b.addEventListener('click', () => alCambiar(destino));
    return b;
  };
  // Ventana de páginas alrededor de la actual.
  const desde = Math.max(0, Math.min(actual - 2, total - 5));
  const hasta = Math.min(total, desde + 5);
  const numeros = [];
  for (let i = desde; i < hasta; i++) {
    numeros.push(boton(String(i + 1), i, `Página ${i + 1}`, i === actual));
  }
  const inicio = actual * pagina.tamano + 1;
  const fin = Math.min((actual + 1) * pagina.tamano, pagina.totalElementos);
  return el('nav', { class: 'paginacion', 'aria-label': 'Paginación' },
    el('p', { class: 'paginacion__info', text: `${numero(inicio)}–${numero(fin)} de ${numero(pagina.totalElementos)}` }),
    el('div', { class: 'paginacion__botones' },
      boton('‹ Anterior', actual - 1, 'Página anterior'),
      numeros,
      boton('Siguiente ›', actual + 1, 'Página siguiente')));
}

// =================================================================== tablas

/**
 * Tabla responsiva: en móvil cada fila se ve como tarjeta (CSS), usando
 * data-etiqueta en cada celda. columnas: [{ titulo, celda(fila) → nodo|texto, clase }].
 */
export function tabla(columnas, filas, { leyenda } = {}) {
  return el('div', { class: 'tabla-contenedor' },
    el('table', { class: 'tabla' },
      leyenda ? el('caption', { class: 'sr-only', text: leyenda }) : null,
      el('thead', {}, el('tr', {}, columnas.map((c) => el('th', { scope: 'col', class: c.clase, text: c.titulo })))),
      el('tbody', {}, filas.map((fila) => el('tr', {},
        columnas.map((c) => el('td', { class: c.clase, 'data-etiqueta': c.titulo }, c.celda(fila))))))));
}

/** Lista de definiciones (dt/dd) para fichas de detalle. */
export function datos(pares) {
  return el('dl', { class: 'datos' },
    pares.filter(Boolean).map(([etiqueta, valor]) => el('div', {}, el('dt', { text: etiqueta }), el('dd', {}, valor ?? '—'))));
}

/** Imagen de herramienta con texto alternativo y alternativa si no hay imagen. */
export function imagenHerramienta(herramienta, clase = 'herramienta__imagen') {
  const url = herramienta.imagenes?.[0]?.url ?? '/img/herramienta-generica.svg';
  return el('img', { class: clase, src: url, alt: herramienta.nombre, loading: 'lazy', width: 400, height: 300 });
}

/**
 * Precio por día. Con descuento: tarifa original tachada, tarifa con descuento y badge "-10 %";
 * sin descuento, solo la tarifa. `props` se aplica al precio vigente (p. ej. un tamaño mayor en la ficha).
 */
export function precioHerramienta(herramienta, props = {}) {
  const vigente = (monto) => el('p', { ...props, class: 'precio' }, cop(monto), el('small', { text: ' / día' }));
  if (!herramienta.descuentoPorcentaje) {
    return vigente(herramienta.tarifaDiaria);
  }
  return el('div', { class: 'precio-descuento' },
    el('s', { class: 'precio__anterior' }, el('span', { class: 'sr-only', text: 'Antes: ' }), cop(herramienta.tarifaDiaria)),
    vigente(herramienta.tarifaConDescuento),
    badge(`-${herramienta.descuentoPorcentaje} %`, 'exito'));
}

/** Tarjeta de herramienta del catálogo: imagen, nombre, precio en COP y estado. */
export function tarjetaHerramienta(herramienta) {
  return el('article', { class: 'tarjeta herramienta' },
    imagenHerramienta(herramienta),
    el('div', { class: 'herramienta__cuerpo' },
      el('h3', { class: 'herramienta__nombre' },
        el('a', { href: `/herramienta.html?id=${encodeURIComponent(herramienta.id)}`, text: herramienta.nombre })),
      herramienta.descripcion
        ? el('p', { class: 'texto-suave texto-sm', text: recortar(herramienta.descripcion, 90) })
        : null,
      herramienta.marca
        ? el('p', { class: 'texto-suave texto-sm', text: recortar(herramienta.marca, 90) })
        : null,
      el('div', { class: 'herramienta__pie' },
        precioHerramienta(herramienta),
        badgeEstado(herramienta.estado))));
}

export function recortar(texto, maximo) {
  const limpio = String(texto ?? '');
  return limpio.length > maximo ? `${limpio.slice(0, maximo - 1).trimEnd()}…` : limpio;
}
