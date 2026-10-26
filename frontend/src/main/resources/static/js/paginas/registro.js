/**
 * Registro público: el selector CLIENTE/PROVEEDOR muestra solo los campos del
 * rol (el otro grupo se deshabilita para que no se valide ni se envíe). Tras
 * registrarse inicia sesión automáticamente.
 */
import { api, ApiError } from '../api.js';
import { guardarSesion, rutaInicio, sesion } from '../auth.js';
import { montarLayout } from '../layout.js';
import {
  $, conCarga, datosFormulario, limpiarErrores, mostrarErrores, toastEnSiguientePagina, validarFormulario,
} from '../ui.js';

const actual = sesion();
if (actual) {
  location.replace(rutaInicio(actual.rol));
} else {
  montarLayout();
  const form = document.getElementById('form-registro');
  const boton = form.querySelector('button[type="submit"]');
  const grupos = {
    CLIENTE: $('#datos-cliente'),
    PROVEEDOR: $('#datos-proveedor'),
  };

  const mostrarRol = () => {
    const rol = form.elements.rol.value;
    for (const [nombre, grupo] of Object.entries(grupos)) {
      const activo = nombre === rol;
      grupo.hidden = !activo;
      grupo.disabled = !activo;
    }
  };
  for (const radio of form.querySelectorAll('input[name="rol"]')) {
    radio.addEventListener('change', mostrarRol);
  }
  mostrarRol();

  form.addEventListener('submit', (evento) => {
    evento.preventDefault();
    limpiarErrores(form);
    const confirmacion = form.elements.confirmacion;
    confirmacion.setCustomValidity(
      confirmacion.value && confirmacion.value !== form.elements.password.value ? 'Las contraseñas no coinciden.' : '');
    if (!validarFormulario(form)) {
      return;
    }
    const datos = datosFormulario(form);
    delete datos.confirmacion;
    conCarga(boton, async () => {
      try {
        await api.postPublico('/auth/registro', datos);
        const login = await api.postPublico('/auth/login', { email: datos.email, password: datos.password });
        guardarSesion(login);
        toastEnSiguientePagina('¡Cuenta creada! Ya puedes usar la plataforma.', 'exito');
        location.replace(rutaInicio(login.rol));
      } catch (error) {
        mostrarErrores(form, error);
        // El email repetido llega como conflicto general: se señala en su campo.
        if (error instanceof ApiError && error.status === 409 && /email/i.test(error.detalle)) {
          const email = form.elements.email;
          email.setAttribute('aria-invalid', 'true');
          $('#email-error').textContent = error.detalle;
          email.focus();
        }
      }
    });
  });
}
