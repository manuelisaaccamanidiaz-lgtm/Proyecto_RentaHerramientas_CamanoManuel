/** Cliente · Mi perfil: nombre, datos de contacto (documento, teléfono, dirección) y contraseña. */
import { api } from '../api.js';
import { requerirSesion } from '../auth.js';
import { seccionCambiarPassword } from '../cambiar-password.js';
import { montarLayout } from '../layout.js';
import {
  badgeEstado, campo, conCarga, datos, datosFormulario, el, estadoError, limpiarErrores, mostrarErrores, reemplazar,
  toast, validarFormulario,
} from '../ui.js';

if (requerirSesion('CLIENTE')) {
  montarLayout();
  cargar();
}

async function cargar() {
  const contenedor = document.getElementById('perfil');
  reemplazar(contenedor, el('div', { class: 'skeleton skeleton--fila' }), el('div', { class: 'skeleton skeleton--fila' }));
  try {
    pintar(contenedor, await api.get('/usuarios/me'));
  } catch (error) {
    reemplazar(contenedor, estadoError(error, cargar));
  }
}

function pintar(contenedor, usuario) {
  const cliente = usuario.datosCliente ?? {};
  const boton = el('button', { class: 'btn btn--primario', type: 'submit' }, el('span', { text: 'Guardar cambios' }));
  const form = el('form', { class: 'formulario', novalidate: true },
    el('div', { class: 'aviso aviso--peligro', role: 'alert', 'data-aviso': '' }),
    campo({ etiqueta: 'Nombre completo', nombre: 'nombre', valor: usuario.nombre, requerido: true, atributos: { maxlength: 120, autocomplete: 'name' } }),
    el('div', { class: 'grupo-campos grupo-campos--2' },
      campo({ etiqueta: 'Documento (cédula)', nombre: 'datosCliente.documento', valor: cliente.documento, requerido: true, ayuda: 'Solo dígitos, de 5 a 15.', atributos: { inputmode: 'numeric', pattern: '[0-9]{5,15}', 'data-mensaje-patron': 'Escribe de 5 a 15 dígitos, sin puntos.', maxlength: 20 } }),
      campo({ etiqueta: 'Teléfono', nombre: 'datosCliente.telefono', tipo: 'tel', valor: cliente.telefono, requerido: true, ayuda: 'Ej.: 3001234567', atributos: { pattern: '\\+?[0-9]{7,15}', 'data-mensaje-patron': 'De 7 a 15 dígitos, con + opcional.', maxlength: 20, autocomplete: 'tel' } })),
    campo({ etiqueta: 'Dirección', nombre: 'datosCliente.direccion', valor: cliente.direccion, requerido: true, atributos: { maxlength: 200, autocomplete: 'street-address' } }),
    el('div', { class: 'acciones' }, boton));

  form.addEventListener('submit', (evento) => {
    evento.preventDefault();
    limpiarErrores(form);
    if (!validarFormulario(form)) {
      return;
    }
    conCarga(boton, async () => {
      try {
        const actualizado = await api.put('/usuarios/me', datosFormulario(form));
        toast('Tus datos se guardaron correctamente.', 'exito');
        pintar(contenedor, actualizado);
      } catch (error) {
        mostrarErrores(form, error);
      }
    });
  });

  reemplazar(contenedor,
    el('section', { class: 'tarjeta pila', 'aria-labelledby': 'titulo-cuenta' },
      el('h2', { id: 'titulo-cuenta', text: 'Tu cuenta' }),
      datos([
        ['Correo', usuario.email],
        ['Tipo de cuenta', badgeEstado(usuario.rol)],
      ]),
      el('p', { class: 'texto-sm texto-suave', text: 'El correo y el tipo de cuenta no se pueden cambiar.' })),
    el('section', { class: 'tarjeta pila', 'aria-labelledby': 'titulo-datos' },
      el('h2', { id: 'titulo-datos', text: 'Datos personales' }),
      form),
    seccionCambiarPassword());
}
