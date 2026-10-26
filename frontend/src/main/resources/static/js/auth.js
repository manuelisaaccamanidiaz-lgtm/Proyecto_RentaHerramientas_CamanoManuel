/**
 * Sesión del usuario en el navegador.
 *
 * El token JWT se guarda en localStorage y su payload se decodifica SOLO para
 * adaptar la interfaz (menú según rol, guards de página). Decodificar no es
 * verificar: la firma la comprueba el backend en cada petición, que sigue
 * siendo la única autoridad sobre qué puede hacer cada usuario.
 */

const CLAVE_TOKEN = 'rentaherramientas.token';

/** Página de inicio de cada rol tras iniciar sesión. */
const INICIO_POR_ROL = {
  CLIENTE: '/cliente/reservas.html',
  PROVEEDOR: '/proveedor/panel.html',
  ADMIN: '/admin/panel.html',
};

function leerToken() {
  try {
    return localStorage.getItem(CLAVE_TOKEN);
  } catch {
    return null;
  }
}

/** Decodifica el payload (base64url) de un JWT; null si no tiene el formato esperado. */
function decodificarPayload(token) {
  const partes = token.split('.');
  if (partes.length !== 3) {
    return null;
  }
  try {
    const base64 = partes[1].replace(/-/g, '+').replace(/_/g, '/');
    const relleno = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');
    const bytes = Uint8Array.from(atob(relleno), (c) => c.charCodeAt(0));
    return JSON.parse(new TextDecoder().decode(bytes));
  } catch {
    return null;
  }
}

/** Guarda el token devuelto por POST /api/auth/login. */
export function guardarSesion(respuestaLogin) {
  try {
    localStorage.setItem(CLAVE_TOKEN, respuestaLogin.token);
  } catch {
    // Sin almacenamiento (modo privado estricto): la sesión dura lo que la página.
  }
}

export function cerrarSesion() {
  try {
    localStorage.removeItem(CLAVE_TOKEN);
  } catch {
    // nada que limpiar
  }
}

/**
 * Sesión vigente: { token, id, rol, email, expira } o null si no hay token o
 * ya expiró (en ese caso se borra para no enviar un token inútil).
 */
export function sesion() {
  const token = leerToken();
  if (!token) {
    return null;
  }
  const payload = decodificarPayload(token);
  if (!payload || !payload.sub || !payload.rol || typeof payload.exp !== 'number') {
    cerrarSesion();
    return null;
  }
  if (payload.exp * 1000 <= Date.now()) {
    cerrarSesion();
    return null;
  }
  return {
    token,
    id: Number(payload.sub),
    rol: payload.rol,
    email: payload.email ?? '',
    expira: new Date(payload.exp * 1000),
  };
}

export function rutaInicio(rol) {
  return INICIO_POR_ROL[rol] ?? '/';
}

/** Solo rutas internas ("/algo"), nunca "//otro-sitio" ni URLs absolutas (evita redirecciones abiertas). */
export function rutaSegura(ruta) {
  return typeof ruta === 'string' && ruta.startsWith('/') && !ruta.startsWith('//') && !ruta.includes('\\')
    ? ruta
    : null;
}

/** Envía al login recordando la página actual para volver después. */
export function irALogin() {
  const actual = location.pathname + location.search;
  location.replace(`/login.html?siguiente=${encodeURIComponent(actual)}`);
}

/**
 * Guard de página. Devuelve la sesión si el usuario tiene uno de los roles; si
 * no hay sesión lo manda al login, y si el rol no corresponde lo manda a su
 * inicio. En esos casos devuelve null y la página no debe seguir cargando.
 */
export function requerirSesion(...roles) {
  const actual = sesion();
  if (!actual) {
    irALogin();
    return null;
  }
  if (roles.length > 0 && !roles.includes(actual.rol)) {
    location.replace(rutaInicio(actual.rol));
    return null;
  }
  return actual;
}
