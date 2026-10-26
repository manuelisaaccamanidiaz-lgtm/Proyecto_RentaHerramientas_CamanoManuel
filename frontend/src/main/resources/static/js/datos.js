/**
 * Cachés en memoria (por página) para datos relacionados que las respuestas
 * solo traen por id: una reserva trae herramientaId, un pago trae reservaId...
 * Cada id se pide una sola vez aunque aparezca en muchas filas.
 */
import { api } from './api.js';

function memoizar(cargar) {
  const cache = new Map();
  return (id) => {
    if (!cache.has(id)) {
      // Si falla, se olvida para poder reintentar después.
      const promesa = cargar(id).catch((error) => {
        cache.delete(id);
        throw error;
      });
      cache.set(id, promesa);
    }
    return cache.get(id);
  };
}

/** Herramienta por id (endpoint público). */
export const herramientaPorId = memoizar((id) => api.get(`/herramientas/${id}`));

/** Reserva por id (la API decide si el usuario puede verla). */
export const reservaPorId = memoizar((id) => api.get(`/reservas/${id}`));

/** Usuario por id: solo para el ADMIN. */
export const usuarioPorId = memoizar((id) => api.get(`/admin/usuarios/${id}`));

let categoriasCache;

/** Todas las categorías (endpoint público, no paginado). */
export function categorias() {
  categoriasCache ??= api.get('/categorias').catch((error) => {
    categoriasCache = undefined;
    throw error;
  });
  return categoriasCache;
}

/** Nombre de una herramienta o un texto de respaldo si no se pudo cargar. */
export async function nombreHerramienta(id) {
  try {
    return (await herramientaPorId(id)).nombre;
  } catch {
    return `Herramienta #${id}`;
  }
}

/** Nombre visible de un usuario (razón social para proveedores) para el ADMIN. */
export async function nombreUsuario(id) {
  try {
    const usuario = await usuarioPorId(id);
    return usuario.datosProveedor?.razonSocial ?? usuario.nombre;
  } catch {
    return `Usuario #${id}`;
  }
}

/** Transiciones de estado permitidas para una herramienta (igual que EstadoHerramienta del dominio). */
export const TRANSICIONES_HERRAMIENTA = {
  DISPONIBLE: ['EN_MANTENIMIENTO', 'DANADA', 'INACTIVA'],
  EN_MANTENIMIENTO: ['DISPONIBLE', 'DANADA', 'INACTIVA'],
  DANADA: ['EN_MANTENIMIENTO', 'INACTIVA'],
  INACTIVA: ['DISPONIBLE'],
};
