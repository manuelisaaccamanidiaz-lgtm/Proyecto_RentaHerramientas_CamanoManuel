/** Admin · Usuarios: filtro por rol, edición de datos, cambio de rol y activación/desactivación (borrado lógico). */
import { api } from '../api.js';
import { requerirSesion } from '../auth.js';
import { montarLayout } from '../layout.js';
import { crearListado, filtrosEnUrl } from '../listado.js';
import { badge, badgeEstado, botonAccion, campo, confirmar, el, modalFormulario, tabla, toast, toastError } from '../ui.js';

const actual = requerirSesion('ADMIN');
if (actual) {
  montarLayout();
  iniciar();
}

function iniciar() {
  const form = document.getElementById('filtros');
  const filtros = filtrosEnUrl(form);

  const listado = crearListado({
    contenedor: document.getElementById('lista'),
    cargar: (pagina) => {
      const valores = filtros.leer();
      filtros.guardar(valores);
      return api.get('/admin/usuarios', { ...valores, page: pagina, size: 10 });
    },
    pintar: (respuesta) => tabla([
      { titulo: 'Nombre', celda: (u) => el('span', {}, el('strong', { text: u.datosProveedor?.razonSocial ?? u.nombre }),
        u.datosProveedor ? el('span', { class: 'texto-suave texto-sm', text: ` · ${u.nombre}` }) : null) },
      { titulo: 'Correo', celda: (u) => u.email },
      { titulo: 'Rol', celda: (u) => badgeEstado(u.rol) },
      { titulo: 'Documento', celda: (u) => u.datosProveedor ? `NIT ${u.datosProveedor.nit}` : u.datosCliente ? `C.C. ${u.datosCliente.documento}` : '—' },
      { titulo: 'Estado', celda: (u) => (u.activo ? badge('Activo', 'exito') : badge('Inactivo', 'neutro')) },
      { titulo: 'Acciones', celda: (u) => acciones(u) },
    ], respuesta.contenido, { leyenda: 'Usuarios' }),
    vacio: { titulo: 'No hay usuarios con ese filtro', texto: 'Prueba con otro rol.' },
  });

  function acciones(usuario) {
    const esUnoMismo = usuario.id === actual.id;
    return el('div', { class: 'acciones' },
      botonAccion('Editar', 'btn--fantasma', () => editar(usuario), { 'aria-label': `Editar a ${usuario.nombre}` }),
      botonAccion('Cambiar rol', 'btn--fantasma', () => cambiarRol(usuario), {
        'aria-label': `Cambiar el rol de ${usuario.nombre}`,
        disabled: esUnoMismo,
        title: esUnoMismo ? 'No puedes cambiar tu propio rol' : null,
      }),
      usuario.activo
        ? botonAccion('Desactivar', 'btn--peligro', () => cambiarEstado(usuario, false), {
          'aria-label': `Desactivar a ${usuario.nombre}`,
          disabled: esUnoMismo,
          title: esUnoMismo ? 'No puedes desactivar tu propia cuenta' : null,
        })
        : botonAccion('Activar', 'btn--primario', () => cambiarEstado(usuario, true), { 'aria-label': `Activar a ${usuario.nombre}` }));
  }

  async function editar(resumen) {
    let usuario;
    try {
      usuario = await api.get(`/admin/usuarios/${resumen.id}`);
    } catch (error) {
      toastError(error);
      return;
    }
    const campos = [
      !usuario.activo
        ? el('p', { class: 'aviso aviso--advertencia', text: 'Este usuario está inactivo: actívalo antes de editar sus datos.' })
        : null,
      campo({ etiqueta: 'Nombre', nombre: 'nombre', valor: usuario.nombre, requerido: true, atributos: { maxlength: 120 } }),
    ];
    if (usuario.datosProveedor) {
      campos.push(el('div', { class: 'grupo-campos grupo-campos--2' },
        campo({ etiqueta: 'Razón social', nombre: 'datosProveedor.razonSocial', valor: usuario.datosProveedor.razonSocial, requerido: true, atributos: { maxlength: 150 } }),
        campo({ etiqueta: 'NIT', nombre: 'datosProveedor.nit', valor: usuario.datosProveedor.nit, requerido: true, ayuda: 'Ej.: 900123456-7', atributos: { maxlength: 20, pattern: '[0-9]{6,10}(-[0-9])?', 'data-mensaje-patron': 'De 6 a 10 dígitos y dígito de verificación opcional (900123456-7).' } })));
    }
    if (usuario.datosCliente) {
      campos.push(el('div', { class: 'grupo-campos grupo-campos--2' },
        campo({ etiqueta: 'Documento', nombre: 'datosCliente.documento', valor: usuario.datosCliente.documento, requerido: true, atributos: { maxlength: 20, pattern: '[0-9]{5,15}', 'data-mensaje-patron': 'De 5 a 15 dígitos.' } }),
        campo({ etiqueta: 'Teléfono', nombre: 'datosCliente.telefono', tipo: 'tel', valor: usuario.datosCliente.telefono, requerido: true, atributos: { maxlength: 20, pattern: '\\+?[0-9]{7,15}', 'data-mensaje-patron': 'De 7 a 15 dígitos, con + opcional.' } })),
      campo({ etiqueta: 'Dirección', nombre: 'datosCliente.direccion', valor: usuario.datosCliente.direccion, requerido: true, atributos: { maxlength: 200 } }));
    }
    const actualizado = await modalFormulario({
      titulo: `Editar · ${usuario.email}`,
      ancho: 640,
      campos,
      alEnviar: (valores) => api.put(`/admin/usuarios/${usuario.id}`, valores),
    });
    if (actualizado) {
      toast('Usuario actualizado.', 'exito');
      await listado.recargar();
    }
  }

  async function cambiarRol(usuario) {
    const opciones = [['CLIENTE', 'Cliente'], ['PROVEEDOR', 'Proveedor'], ['ADMIN', 'Administrador']]
      .filter(([rol]) => rol !== usuario.rol);
    const selectorRol = campo({ etiqueta: 'Nuevo rol', nombre: 'rol', tipo: 'select', valor: opciones[0][0], requerido: true, opciones });
    // Un <fieldset disabled> no se valida ni se envía (datosFormulario lo omite): así solo viaja el bloque del rol elegido.
    // El fieldset va sin clase: .grupo-campos fija display:grid y anularía el atributo hidden.
    const grupoProveedor = el('fieldset', {},
      el('div', { class: 'grupo-campos grupo-campos--2' },
        campo({ etiqueta: 'Razón social', nombre: 'datosProveedor.razonSocial', requerido: true, atributos: { maxlength: 150 } }),
        campo({ etiqueta: 'NIT', nombre: 'datosProveedor.nit', requerido: true, ayuda: 'Ej.: 900123456-7', atributos: { maxlength: 20, pattern: '[0-9]{6,10}(-[0-9])?', 'data-mensaje-patron': 'De 6 a 10 dígitos y dígito de verificación opcional (900123456-7).' } })));
    const grupoCliente = el('fieldset', {},
      el('div', { class: 'grupo-campos' },
        el('div', { class: 'grupo-campos grupo-campos--2' },
          campo({ etiqueta: 'Documento', nombre: 'datosCliente.documento', requerido: true, atributos: { maxlength: 20, pattern: '[0-9]{5,15}', 'data-mensaje-patron': 'De 5 a 15 dígitos.' } }),
          campo({ etiqueta: 'Teléfono', nombre: 'datosCliente.telefono', tipo: 'tel', requerido: true, atributos: { maxlength: 20, pattern: '\\+?[0-9]{7,15}', 'data-mensaje-patron': 'De 7 a 15 dígitos, con + opcional.' } })),
        campo({ etiqueta: 'Dirección', nombre: 'datosCliente.direccion', requerido: true, atributos: { maxlength: 200 } })));

    const select = selectorRol.querySelector('select');
    const mostrarGrupos = () => {
      for (const [grupo, rol] of [[grupoProveedor, 'PROVEEDOR'], [grupoCliente, 'CLIENTE']]) {
        const activo = select.value === rol;
        grupo.hidden = !activo;
        grupo.disabled = !activo;
      }
    };
    select.addEventListener('change', mostrarGrupos);
    mostrarGrupos();

    const actualizado = await modalFormulario({
      titulo: `Cambiar rol · ${usuario.email}`,
      ancho: 640,
      campos: [
        el('p', { class: 'aviso aviso--advertencia', text: `Rol actual: ${usuario.rol}. Los datos del rol actual se eliminarán. No es posible si el usuario ya tiene reservas o herramientas registradas.` }),
        selectorRol,
        grupoProveedor,
        grupoCliente,
      ],
      textoEnviar: 'Cambiar rol',
      alEnviar: (valores) => api.patch(`/admin/usuarios/${usuario.id}/rol`, valores),
    });
    if (actualizado) {
      toast('Rol actualizado. El usuario debe volver a iniciar sesión.', 'exito');
      await listado.recargar();
    }
  }

  async function cambiarEstado(usuario, activar) {
    const ok = await confirmar(activar
      ? { titulo: 'Activar usuario', mensaje: `${usuario.nombre} podrá volver a iniciar sesión.`, textoConfirmar: 'Activar' }
      : {
        titulo: 'Desactivar usuario',
        mensaje: `${usuario.nombre} no podrá iniciar sesión. Sus reservas, herramientas e historial se conservan (borrado lógico).`,
        textoConfirmar: 'Desactivar',
        peligro: true,
      });
    if (!ok) {
      return;
    }
    try {
      await api.patch(`/admin/usuarios/${usuario.id}/${activar ? 'activar' : 'desactivar'}`);
      toast(activar ? 'Usuario activado.' : 'Usuario desactivado.', 'exito');
      await listado.recargar();
    } catch (error) {
      toastError(error);
    }
  }

  form.addEventListener('change', () => listado.reiniciar());
  listado.recargar(0);
}
