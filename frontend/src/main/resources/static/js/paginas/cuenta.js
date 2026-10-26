/** Proveedor y admin · Mi cuenta: correo, tipo de cuenta y cambio de contraseña (el cliente lo tiene en Mi perfil). */
import { api } from '../api.js';
import { requerirSesion } from '../auth.js';
import { seccionCambiarPassword } from '../cambiar-password.js';
import { montarLayout } from '../layout.js';
import { badgeEstado, datos, el, estadoError, reemplazar } from '../ui.js';

if (requerirSesion('PROVEEDOR', 'ADMIN')) {
  montarLayout();
  cargar();
}

async function cargar() {
  const contenedor = document.getElementById('cuenta');
  reemplazar(contenedor, el('div', { class: 'skeleton skeleton--fila' }));
  try {
    const usuario = await api.get('/usuarios/me');
    reemplazar(contenedor,
      el('section', { class: 'tarjeta pila', 'aria-labelledby': 'titulo-cuenta' },
        el('h2', { id: 'titulo-cuenta', text: 'Tu cuenta' }),
        datos([
          ['Nombre', usuario.nombre],
          ['Correo', usuario.email],
          ['Tipo de cuenta', badgeEstado(usuario.rol)],
        ])),
      seccionCambiarPassword());
  } catch (error) {
    reemplazar(contenedor, estadoError(error, cargar));
  }
}
