/**
 * Sección "Cambiar contraseña", común a todos los roles (Mi perfil del cliente
 * y Mi cuenta del proveedor y el admin). La confirmación solo se valida aquí;
 * al backend viajan la actual y la nueva.
 */
import { api } from './api.js';
import { campo, conCarga, el, limpiarErrores, mostrarErrores, toast, validarFormulario } from './ui.js';

export function seccionCambiarPassword() {
  const boton = el('button', { class: 'btn btn--primario', type: 'submit' }, el('span', { text: 'Cambiar contraseña' }));
  const form = el('form', { class: 'formulario', novalidate: true },
    el('div', { class: 'aviso aviso--peligro', role: 'alert', 'data-aviso': '' }),
    campo({ etiqueta: 'Contraseña actual', nombre: 'passwordActual', tipo: 'password', requerido: true, atributos: { maxlength: 72, autocomplete: 'current-password' } }),
    el('div', { class: 'grupo-campos grupo-campos--2' },
      campo({ etiqueta: 'Contraseña nueva', nombre: 'passwordNueva', tipo: 'password', requerido: true, ayuda: 'De 8 a 72 caracteres.', atributos: { minlength: 8, maxlength: 72, autocomplete: 'new-password' } }),
      campo({ etiqueta: 'Confirmar contraseña nueva', nombre: 'confirmarPassword', tipo: 'password', requerido: true, atributos: { minlength: 8, maxlength: 72, autocomplete: 'new-password' } })),
    el('div', { class: 'acciones' }, boton));

  const nueva = form.elements.passwordNueva;
  const confirmar = form.elements.confirmarPassword;
  // validarFormulario muestra el validationMessage, así que el "no coinciden" sale como error del campo.
  const revisarCoincidencia = () => confirmar.setCustomValidity(
    confirmar.value && confirmar.value !== nueva.value ? 'Las contraseñas no coinciden.' : '');
  nueva.addEventListener('input', revisarCoincidencia);
  confirmar.addEventListener('input', revisarCoincidencia);

  form.addEventListener('submit', (evento) => {
    evento.preventDefault();
    limpiarErrores(form);
    revisarCoincidencia();
    if (!validarFormulario(form)) {
      return;
    }
    conCarga(boton, async () => {
      try {
        // Sin datosFormulario: recorta espacios y una contraseña puede empezar o terminar con uno.
        await api.patch('/usuarios/me/password', {
          passwordActual: form.elements.passwordActual.value,
          passwordNueva: nueva.value,
        });
        toast('Tu contraseña se cambió correctamente.', 'exito');
        form.reset();
      } catch (error) {
        mostrarErrores(form, error);
      }
    });
  });

  return el('section', { class: 'tarjeta pila', 'aria-labelledby': 'titulo-password' },
    el('h2', { id: 'titulo-password', text: 'Cambiar contraseña' }),
    form);
}
