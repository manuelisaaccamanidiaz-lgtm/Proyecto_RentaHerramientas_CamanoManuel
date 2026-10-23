/** Cliente · Mis favoritos: herramientas marcadas con ❤️, la más reciente primero. */
import { api } from '../api.js';
import { requerirSesion } from '../auth.js';
import { botonFavorito } from '../favoritos.js';
import { montarLayout } from '../layout.js';
import { crearListado } from '../listado.js';
import { el, tarjetaHerramienta } from '../ui.js';

if (requerirSesion('CLIENTE')) {
  montarLayout();
  iniciar();
}

function iniciar() {
  const listado = crearListado({
    contenedor: document.getElementById('lista'),
    tipoEsqueleto: 'tarjetas',
    cargar: (pagina) => api.get('/favoritos/mios', { page: pagina, size: 12 }),
    pintar: (respuesta) => el('div', { class: 'rejilla rejilla-3' }, respuesta.contenido.map((herramienta) => {
      const tarjeta = tarjetaHerramienta(herramienta);
      // Al desmarcar, la tarjeta sale de la lista (y si era la última de la página, retrocede una).
      tarjeta.append(botonFavorito(herramienta, { marcado: true, alCambiar: () => listado.recargar() }));
      return tarjeta;
    })),
    vacio: {
      titulo: 'Aún no tienes favoritos',
      texto: 'Toca el corazón 🤍 de una herramienta en el catálogo para guardarla aquí.',
      accion: { texto: 'Explorar el catálogo', href: '/catalogo.html' },
    },
  });

  listado.recargar(0);
}
