/**
 * Cliente HTTP de la API (mismo origen, base relativa "/api").
 *
 * - Agrega "Authorization: Bearer <token>" si hay sesión.
 * - Convierte las respuestas de error (ProblemDetail, RFC 9457) en ApiError con
 *   status, título, detalle y el mapa "errores" de las validaciones por campo.
 * - Ante un 401 cierra la sesión y lleva al login (salvo en el propio login).
 */
import { cerrarSesion, irALogin, sesion } from './auth.js';

const BASE = '/api';

export class ApiError extends Error {
  constructor(status, problema = {}) {
    super(problema.detail || problema.title || `Error ${status}`);
    this.name = 'ApiError';
    this.status = status;
    this.titulo = problema.title || 'Error';
    this.detalle = problema.detail || 'Ocurrió un error inesperado.';
    /** { campo: mensaje } cuando el backend rechazó campos concretos (400). */
    this.errores = problema.errores || {};
  }
}

function construirUrl(ruta, params) {
  const url = new URL(BASE + ruta, location.origin);
  if (params) {
    for (const [clave, valor] of Object.entries(params)) {
      if (valor !== undefined && valor !== null && valor !== '') {
        url.searchParams.set(clave, valor);
      }
    }
  }
  return url.pathname + url.search;
}

async function leerProblema(respuesta) {
  const tipo = respuesta.headers.get('Content-Type') || '';
  if (tipo.includes('json')) {
    try {
      return await respuesta.json();
    } catch {
      return {};
    }
  }
  return {};
}

async function solicitar(metodo, ruta, { params, cuerpo, formData, accept = 'application/json', publica = false } = {}) {
  const cabeceras = { Accept: accept };
  const actual = sesion();
  if (actual && !publica) {
    cabeceras.Authorization = `Bearer ${actual.token}`;
  }
  let body;
  if (formData) {
    body = formData; // el navegador pone el Content-Type multipart con su boundary
  } else if (cuerpo !== undefined) {
    cabeceras['Content-Type'] = 'application/json';
    body = JSON.stringify(cuerpo);
  }

  let respuesta;
  try {
    respuesta = await fetch(construirUrl(ruta, params), { method: metodo, headers: cabeceras, body });
  } catch {
    throw new ApiError(0, {
      title: 'Sin conexión',
      detail: 'No se pudo contactar con el servidor. Revisa tu conexión e inténtalo de nuevo.',
    });
  }

  if (respuesta.status === 401 && !publica) {
    // Token vencido o inválido: la sesión del navegador ya no sirve.
    cerrarSesion();
    irALogin();
    throw new ApiError(401, { title: 'Sesión expirada', detail: 'Tu sesión expiró. Inicia sesión de nuevo.' });
  }
  if (!respuesta.ok) {
    throw new ApiError(respuesta.status, await leerProblema(respuesta));
  }
  if (respuesta.status === 204) {
    return null;
  }
  if (accept !== 'application/json') {
    return respuesta;
  }
  const tipo = respuesta.headers.get('Content-Type') || '';
  return tipo.includes('json') ? respuesta.json() : null;
}

/** Nombre de archivo del header Content-Disposition (filename="x.pdf"). */
function nombreDeArchivo(respuesta, porDefecto) {
  const disposicion = respuesta.headers.get('Content-Disposition') || '';
  const coincidencia = /filename\*?=(?:UTF-8'')?"?([^";]+)"?/i.exec(disposicion);
  return coincidencia ? decodeURIComponent(coincidencia[1]) : porDefecto;
}

export const api = {
  get: (ruta, params) => solicitar('GET', ruta, { params }),
  /** Para endpoints públicos que no deben enviar el token (login, registro). */
  postPublico: (ruta, cuerpo) => solicitar('POST', ruta, { cuerpo, publica: true }),
  post: (ruta, cuerpo) => solicitar('POST', ruta, { cuerpo }),
  put: (ruta, cuerpo) => solicitar('PUT', ruta, { cuerpo }),
  patch: (ruta, cuerpo) => solicitar('PATCH', ruta, { cuerpo }),
  eliminar: (ruta) => solicitar('DELETE', ruta),
  subir: (ruta, formData) => solicitar('POST', ruta, { formData }),

  /** Descarga un archivo (p. ej. el PDF de una factura) como blob y lo guarda. */
  async descargar(ruta, nombrePorDefecto = 'archivo.pdf') {
    const respuesta = await solicitar('GET', ruta, { accept: 'application/pdf' });
    const blob = await respuesta.blob();
    const url = URL.createObjectURL(blob);
    const enlace = document.createElement('a');
    enlace.href = url;
    enlace.download = nombreDeArchivo(respuesta, nombrePorDefecto);
    document.body.append(enlace);
    enlace.click();
    enlace.remove();
    // Se libera después de que el navegador haya iniciado la descarga.
    setTimeout(() => URL.revokeObjectURL(url), 10000);
  },
};
