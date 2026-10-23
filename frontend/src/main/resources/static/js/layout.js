/**
 * Estructura común de todas las páginas: navbar responsiva (menú hamburguesa en
 * móvil) con los enlaces según el rol de la sesión, pie de página y región de
 * notificaciones. Cada página llama a montarLayout() al cargar.
 */
import { cerrarSesion, sesion } from './auth.js';
import { confirmar, el, mostrarToastPendiente, textoEstado } from './ui.js';

const MENUS = {
  PUBLICO: [
    ['Inicio', '/'],
    ['Catálogo', '/catalogo.html'],
  ],
  CLIENTE: [
    ['Catálogo', '/catalogo.html'],
    ['Mis reservas', '/cliente/reservas.html'],
    ['Mis pagos', '/cliente/pagos.html'],
    ['Mi perfil', '/cliente/perfil.html'],
  ],
  PROVEEDOR: [
    ['Panel', '/proveedor/panel.html'],
    ['Inventario', '/proveedor/inventario.html'],
    ['Solicitudes', '/proveedor/solicitudes.html'],
    ['Facturas', '/proveedor/facturas.html'],
    ['Catálogo', '/catalogo.html'],
    ['Mi cuenta', '/cuenta.html'],
  ],
  ADMIN: [
    ['Panel', '/admin/panel.html'],
    ['Usuarios', '/admin/usuarios.html'],
    ['Categorías', '/admin/categorias.html'],
    ['Herramientas', '/admin/herramientas.html'],
    ['Reservas', '/admin/reservas.html'],
    ['Pagos', '/admin/pagos.html'],
    ['Mi cuenta', '/cuenta.html'],
  ],
};

/** Logo: llave sobre un escudo naranja (marcado constante, sin datos del servidor). */
const LOGO = `
<svg class="navbar__logo" viewBox="0 0 32 32" aria-hidden="true">
  <rect width="32" height="32" rx="8" fill="#F97316"/>
  <path d="M21.5 6.5a5 5 0 0 0-6.6 6.2l-7.6 7.6a2.2 2.2 0 0 0 3.1 3.1l7.6-7.6a5 5 0 0 0 6.2-6.6l-3 3-2.7-.6-.6-2.7z" fill="#1F2937"/>
</svg>`;

const ICONO_MENU = `
<svg viewBox="0 0 24 24" aria-hidden="true" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
  <path d="M4 7h16M4 12h16M4 17h16"/>
</svg>`;

function svg(marcado) {
  const plantilla = document.createElement('template');
  plantilla.innerHTML = marcado.trim();
  return plantilla.content.firstElementChild;
}

function esPaginaActual(href) {
  const ruta = location.pathname === '/' ? '/' : location.pathname;
  return href === ruta || (href === '/' && ruta === '/index.html');
}

function navbar() {
  const actual = sesion();
  const enlaces = MENUS[actual?.rol ?? 'PUBLICO'] ?? MENUS.PUBLICO;

  const lista = el('ul', { class: 'navbar__enlaces', role: 'list' },
    enlaces.map(([texto, href]) => el('li', {},
      el('a', { class: 'navbar__enlace', href, text: texto, 'aria-current': esPaginaActual(href) ? 'page' : null }))));

  let usuario;
  if (actual) {
    const salir = el('button', { class: 'btn btn--fantasma btn--sm', type: 'button', text: 'Cerrar sesión' });
    salir.addEventListener('click', async () => {
      const ok = await confirmar({ titulo: 'Cerrar sesión', mensaje: '¿Seguro que quieres cerrar tu sesión?', textoConfirmar: 'Cerrar sesión' });
      if (ok) {
        cerrarSesion();
        location.href = '/';
      }
    });
    usuario = el('div', { class: 'navbar__usuario' },
      el('span', { class: 'navbar__nombre', title: `${actual.email} · ${textoEstado(actual.rol)}` }, el('span', { class: 'sr-only', text: 'Sesión iniciada como ' }),
        actual.email, ` · ${textoEstado(actual.rol)}`),
      salir);
  } else {
    usuario = el('div', { class: 'navbar__usuario' },
      el('a', { class: 'btn btn--fantasma btn--sm', href: '/login.html', text: 'Ingresar' }),
      el('a', { class: 'btn btn--primario btn--sm', href: '/registro.html', text: 'Crear cuenta' }));
  }

  const menu = el('div', { class: 'navbar__menu', id: 'menu-principal' },
    el('nav', { 'aria-label': 'Principal' }, lista), usuario);

  const toggle = el('button', {
    class: 'navbar__toggle', type: 'button', 'aria-controls': 'menu-principal', 'aria-expanded': 'false',
    'aria-label': 'Abrir menú',
  }, svg(ICONO_MENU));
  const alternar = (abrir) => {
    menu.classList.toggle('abierto', abrir);
    toggle.setAttribute('aria-expanded', String(abrir));
    toggle.setAttribute('aria-label', abrir ? 'Cerrar menú' : 'Abrir menú');
  };
  toggle.addEventListener('click', () => alternar(!menu.classList.contains('abierto')));
  document.addEventListener('keydown', (evento) => {
    if (evento.key === 'Escape' && menu.classList.contains('abierto')) {
      alternar(false);
      toggle.focus();
    }
  });

  const marca = el('a', { class: 'navbar__marca', href: '/' }, svg(LOGO), el('span', { text: 'RentaHerramientas' }));
  return el('div', { class: 'contenedor' },
    el('div', { class: 'navbar__contenido' },
      el('div', { class: 'navbar__barra' }, marca, toggle),
      menu));
}

function pie() {
  return el('div', { class: 'contenedor fila-entre' },
    el('p', {}, el('strong', { text: 'RentaHerramientas' }), ' · Alquiler de herramientas en Bucaramanga y su área metropolitana.'),
    el('p', { text: 'Pagos simulados con fines académicos.' }));
}

/** Monta navbar, pie y región de toasts. Las páginas tienen <header id="encabezado"> y <footer id="pie">. */
export function montarLayout() {
  const encabezado = document.getElementById('encabezado');
  if (encabezado) {
    encabezado.className = 'navbar';
    encabezado.replaceChildren(navbar());
  }
  const piePagina = document.getElementById('pie');
  if (piePagina) {
    piePagina.className = 'pie';
    piePagina.replaceChildren(pie());
  }
  mostrarToastPendiente();
}
