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
 * Funciona en cualquier ancho porque el texto no compite con las barras.
 * datos: [{ etiqueta, valor }], formato: valor → texto.
 */
export function graficaBarras(datos, { formato = String, titulo = 'Gráfica de barras', secundaria = false } = {}) {
  if (!datos.length) {
    return sinDatos();
  }
  const ancho = 600;
  const altoFila = 46;
  const alto = datos.length * altoFila;
  const maximo = Math.max(...datos.map((d) => Number(d.valor)), 1);
  const anchoBarra = ancho - 110;

  const svg = nodo('svg', { viewBox: `0 0 ${ancho} ${alto}`, role: 'img', 'aria-label': titulo });
  datos.forEach((dato, i) => {
    const y = i * altoFila;
    const largo = Math.max(2, (Number(dato.valor) / maximo) * anchoBarra);
    svg.append(nodo('text', { x: 0, y: y + 14, class: 'grafica__texto' }, recortar(dato.etiqueta, 70)));
    const barra = nodo('rect', {
      x: 0, y: y + 20, width: largo, height: 16, rx: 4,
      class: `grafica__barra${secundaria ? ' grafica__barra--secundaria' : ''}`,
    });
    barra.append(nodo('title', {}, `${dato.etiqueta}: ${formato(dato.valor)}`));
    svg.append(barra);
    svg.append(nodo('text', { x: largo + 8, y: y + 33, class: 'grafica__valor' }, formato(dato.valor)));
  });
  return el('div', { class: 'grafica' }, svg);
}

/**
 * Línea con área sombreada, ejes y guías horizontales.
 * datos: [{ etiqueta, valor }] en orden cronológico.
 */
export function graficaLineas(datos, { formato = String, formatoEje = formato, titulo = 'Gráfica de líneas' } = {}) {
  if (!datos.length) {
    return sinDatos();
  }
  const ancho = 640;
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
  const cada = Math.ceil(datos.length / 8);
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
