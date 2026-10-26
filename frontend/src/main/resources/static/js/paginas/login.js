/** Inicio de sesión: guarda el token y vuelve a la página pedida o al inicio del rol. */
import { api, ApiError } from '../api.js';
import { guardarSesion, rutaInicio, rutaSegura, sesion } from '../auth.js';
import { montarLayout } from '../layout.js';
import { conCarga, datosFormulario, limpiarErrores, mostrarErrores, parametrosUrl, validarFormulario } from '../ui.js';

const siguiente = rutaSegura(parametrosUrl().siguiente);
const actual = sesion();
if (actual) {
  location.replace(siguiente ?? rutaInicio(actual.rol));
} else {
  montarLayout();
  const form = document.getElementById('form-login');
  const boton = form.querySelector('button[type="submit"]');

  if (siguiente) {
    document.getElementById('aviso-siguiente').textContent = 'Inicia sesión para continuar.';
  }

  form.addEventListener('submit', (evento) => {
    evento.preventDefault();
    limpiarErrores(form);
    if (!validarFormulario(form)) {
      return;
    }
    conCarga(boton, async () => {
      try {
        const respuesta = await api.postPublico('/auth/login', datosFormulario(form));
        guardarSesion(respuesta);
        location.replace(siguiente ?? rutaInicio(respuesta.rol));
      } catch (error) {
        if (error instanceof ApiError && error.status === 401) {
          form.querySelector('[data-aviso]').textContent = 'Correo o contraseña incorrectos.';
          form.elements.password.value = '';
          form.elements.password.focus();
        } else {
          mostrarErrores(form, error);
        }
      }
    });
  });
}
