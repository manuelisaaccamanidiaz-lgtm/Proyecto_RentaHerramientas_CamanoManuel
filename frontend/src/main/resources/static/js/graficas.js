/**
 * Gráficas hechas a mano con SVG (sin librerías). Todos los textos se insertan
 * con textContent: las etiquetas vienen del servidor (nombres de herramientas,
 * clientes...). Cada gráfica lleva role="img" con una descripción para lectores
 * de pantalla y <title> en cada barra o punto para ver el valor exacto.
 */
import { el, recortar } from './ui.js';

const SVG = 'http://www.w3.org/2000/svg';

function nodo(etiqueta, atributos = {}, texto) {
  const n = document.createElementNS(SVG, etiqueta);
  for (const [clave, valor] of Object.entries(atributos)) {
    n.setAttribute(clave, String(valor));
  }
  if (texto !== undefined) {
    n.textContent = String(texto);
  }
  return n;
}

function sinDatos(texto = 'Aún no hay datos para esta gráfica.') {
  return el('p', { class: 'texto-suave texto-sm', text: texto });
}

/**
 * Barras horizontales: cada fila tiene etiqueta encima, barra y valor al final.
 * Se dibujan con HTML (no SVG) para que el texto conserve su tamaño real en
 * cualquier ancho; un SVG escalado lo dejaba ilegible en móvil.
 * datos: [{ etiqueta, valor }], formato: valor → texto.
 */
export function graficaBarras(datos, { formato = String, titulo = 'Gráfica de barras', secundaria = false } = {}) {
  if (!datos.length) {
    return sinDatos();
  }
  const maximo = Math.max(...datos.map((d) => Number(d.valor)), 1);
  const filas = datos.map((dato) => {
    // Proporción sobre el ancho que queda tras reservar 7rem para el valor.
    const proporcion = Math.max(0.01, Number(dato.valor) / maximo);
    const barra = el('span', {
      class: `grafica__barra${secundaria ? ' grafica__barra--secundaria' : ''}`,
      style: `width:calc((100% - 7rem) * ${proporcion.toFixed(4)})`,
      title: `${dato.etiqueta}: ${formato(dato.valor)}`,
    });
    return el('li', { class: 'grafica__fila' },
      el('span', { class: 'grafica__texto', text: recortar(dato.etiqueta, 70) }),
      el('span', { class: 'grafica__pista' }, barra, el('span', { class: 'grafica__valor', text: formato(dato.valor) })));
  });
  return el('div', { class: 'grafica' }, el('ul', { class: 'grafica__barras', role: 'list', 'aria-label': titulo }, filas));
}

/**
 * Línea con área sombreada, ejes y guías horizontales.
 * datos: [{ etiqueta, valor }] en orden cronológico.
 */
export function graficaLineas(datos, { formato = String, formatoEje = formato, titulo = 'Gráfica de líneas' } = {}) {
  if (!datos.length) {
    return sinDatos();
  }
  // El viewBox sigue el ancho disponible (ventana menos márgenes de página y tarjeta)
  // para que el texto del SVG no se encoja en pantallas angostas.
  const ancho = Math.round(Math.min(640, Math.max(300, window.innerWidth - 72)));
  const alto = 280;
  const margen = { arriba: 20, derecha: 20, abajo: 36, izquierda: 72 };
  const anchoUtil = ancho - margen.izquierda - margen.derecha;
  const altoUtil = alto - margen.arriba - margen.abajo;
  const maximo = Math.max(...datos.map((d) => Number(d.valor)), 1) * 1.1;
  const x = (i) => margen.izquierda + (datos.length === 1 ? anchoUtil / 2 : (i / (datos.length - 1)) * anchoUtil);
  const y = (v) => margen.arriba + altoUtil - (Number(v) / maximo) * altoUtil;

  const svg = nodo('svg', { viewBox: `0 0 ${ancho} ${alto}`, role: 'img', 'aria-label': titulo });

  // Guías horizontales y valores del eje Y.
  const pasos = 4;
  for (let p = 0; p <= pasos; p++) {
    const valor = (maximo / pasos) * p;
    const yy = y(valor);
    svg.append(nodo('line', { x1: margen.izquierda, x2: ancho - margen.derecha, y1: yy, y2: yy, class: p === 0 ? 'grafica__eje' : 'grafica__guia' }));
    svg.append(nodo('text', { x: margen.izquierda - 8, y: yy + 4, 'text-anchor': 'end', class: 'grafica__texto' }, formatoEje(valor)));
  }

  const puntos = datos.map((d, i) => [x(i), y(d.valor)]);
  const linea = puntos.map(([px, py]) => `${px},${py}`).join(' ');
  const base = y(0);
  svg.append(nodo('polygon', {
    points: `${puntos[0][0]},${base} ${linea} ${puntos.at(-1)[0]},${base}`, class: 'grafica__area',
  }));
  svg.append(nodo('polyline', { points: linea, class: 'grafica__linea' }));

  // Etiquetas del eje X: todas si caben, si no una de cada n.
  const cada = Math.ceil(datos.length / (ancho < 480 ? 4 : 8));
  datos.forEach((dato, i) => {
    const [px, py] = puntos[i];
    const punto = nodo('circle', { cx: px, cy: py, r: 5, class: 'grafica__punto' });
    punto.append(nodo('title', {}, `${dato.etiqueta}: ${formato(dato.valor)}`));
    svg.append(punto);
    if (i % cada === 0 || i === datos.length - 1) {
      svg.append(nodo('text', { x: px, y: alto - 12, 'text-anchor': 'middle', class: 'grafica__texto' }, dato.etiqueta));
    }
  });
  return el('div', { class: 'grafica' }, svg);
}
