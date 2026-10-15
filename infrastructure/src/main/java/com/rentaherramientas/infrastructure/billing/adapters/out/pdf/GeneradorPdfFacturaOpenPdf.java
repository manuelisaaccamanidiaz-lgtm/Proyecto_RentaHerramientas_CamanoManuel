package com.rentaherramientas.infrastructure.billing.adapters.out.pdf;

import com.rentaherramientas.application.billing.dto.FacturaResponse;
import com.rentaherramientas.application.billing.port.DocumentoFactura;
import com.rentaherramientas.application.billing.port.GeneradorPdfFactura;
import com.rentaherramientas.application.billing.port.Tercero;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Adaptador de salida: dibuja la factura con OpenPDF. Encabezado con la
 * plataforma, número y fecha; recuadros de cliente y proveedor; tabla de líneas;
 * total en formato COP ("$ 1.234.567") y la referencia del pago. Usa las fuentes
 * estándar de PDF (Helvetica, codificación WinAnsi), que cubren tildes y ñ sin
 * incrustar archivos de fuentes.
 */
@Component
public class GeneradorPdfFacturaOpenPdf implements GeneradorPdfFactura {

    static final String PLATAFORMA = "Renta de Herramientas";
    private static final Locale COLOMBIA = Locale.of("es", "CO");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color AZUL = new Color(0x1F, 0x4E, 0x79);
    private static final Color GRIS_CLARO = new Color(0xF2, 0xF2, 0xF2);
    private static final Color GRIS_BORDE = new Color(0xBF, 0xBF, 0xBF);

    private static final Font TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.NORMAL, AZUL);
    private static final Font SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Font.NORMAL, AZUL);
    private static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, Color.BLACK);
    private static final Font NEGRITA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.NORMAL, Color.BLACK);
    private static final Font CABECERA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.NORMAL, Color.WHITE);
    private static final Font TOTAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Font.NORMAL, AZUL);
    private static final Font PIE = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Font.NORMAL, Color.GRAY);

    @Override
    public byte[] generar(DocumentoFactura documento) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document pdf = new Document(PageSize.LETTER, 40, 40, 40, 40);
        try {
            PdfWriter.getInstance(pdf, salida);
            FacturaResponse factura = documento.factura();
            pdf.addTitle("Factura " + factura.numero());
            pdf.addAuthor(PLATAFORMA);
            pdf.open();
            pdf.add(encabezado(factura));
            pdf.add(terceros(documento.cliente(), documento.proveedor()));
            pdf.add(lineas(factura));
            pdf.add(totales(factura));
            Paragraph pago = new Paragraph("Pago " + documento.metodoPago() + " · referencia "
                    + documento.referenciaPago(), NORMAL);
            pago.setSpacingBefore(12);
            pdf.add(pago);
            Paragraph pie = new Paragraph("Documento generado por " + PLATAFORMA
                    + ". Pago simulado con fines académicos: no constituye factura electrónica DIAN.", PIE);
            pie.setSpacingBefore(24);
            pdf.add(pie);
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el PDF de la factura " + documento.factura().numero(), e);
        } finally {
            if (pdf.isOpen()) {
                pdf.close();
            }
        }
        return salida.toByteArray();
    }

    private static PdfPTable encabezado(FacturaResponse factura) throws DocumentException {
        PdfPTable tabla = new PdfPTable(new float[]{3, 2});
        tabla.setWidthPercentage(100);
        PdfPCell plataforma = celdaSinBorde(null);
        plataforma.addElement(new Paragraph(PLATAFORMA, TITULO));
        plataforma.addElement(new Paragraph("Alquiler de herramientas entre proveedores y clientes", NORMAL));
        tabla.addCell(plataforma);

        PdfPCell datos = celdaSinBorde(null);
        datos.addElement(alineado(new Paragraph("FACTURA DE VENTA", SUBTITULO)));
        datos.addElement(alineado(new Paragraph("N.º " + factura.numero(), NEGRITA)));
        datos.addElement(alineado(new Paragraph("Fecha de emisión: " + factura.fechaEmision().format(FECHA_HORA),
                NORMAL)));
        datos.addElement(alineado(new Paragraph("Moneda: " + factura.moneda(), NORMAL)));
        tabla.addCell(datos);
        tabla.setSpacingAfter(16);
        return tabla;
    }

    private static PdfPTable terceros(Tercero cliente, Tercero proveedor) throws DocumentException {
        PdfPTable tabla = new PdfPTable(new float[]{1, 1});
        tabla.setWidthPercentage(100);
        tabla.addCell(recuadroTercero("PROVEEDOR", proveedor));
        tabla.addCell(recuadroTercero("CLIENTE", cliente));
        tabla.setSpacingAfter(16);
        return tabla;
    }

    private static PdfPCell recuadroTercero(String titulo, Tercero tercero) {
        PdfPCell celda = new PdfPCell();
        celda.setBorderColor(GRIS_BORDE);
        celda.setPadding(8);
        celda.addElement(new Paragraph(titulo, SUBTITULO));
        celda.addElement(new Paragraph(tercero.nombre(), NEGRITA));
        if (tercero.numeroDocumento() != null) {
            celda.addElement(new Paragraph(tercero.tipoDocumento() + " " + tercero.numeroDocumento(), NORMAL));
        }
        if (tercero.email() != null) {
            celda.addElement(new Paragraph(tercero.email(), NORMAL));
        }
        if (tercero.telefono() != null) {
            celda.addElement(new Paragraph("Tel. " + tercero.telefono(), NORMAL));
        }
        if (tercero.direccion() != null) {
            celda.addElement(new Paragraph(tercero.direccion(), NORMAL));
        }
        return celda;
    }

    private static PdfPTable lineas(FacturaResponse factura) throws DocumentException {
        PdfPTable tabla = new PdfPTable(new float[]{6, 1.2f, 2, 2});
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1);
        for (String titulo : new String[]{"Descripción", "Cantidad (días)", "Valor unitario", "Subtotal"}) {
            PdfPCell cabecera = new PdfPCell(new Phrase(titulo, CABECERA));
            cabecera.setBackgroundColor(AZUL);
            cabecera.setBorderColor(AZUL);
            cabecera.setPadding(6);
            cabecera.setHorizontalAlignment(titulo.equals("Descripción") ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
            tabla.addCell(cabecera);
        }
        boolean par = false;
        for (FacturaResponse.LineaResponse linea : factura.lineas()) {
            Color fondo = par ? GRIS_CLARO : Color.WHITE;
            tabla.addCell(celdaLinea(linea.descripcion(), Element.ALIGN_LEFT, fondo));
            tabla.addCell(celdaLinea(String.valueOf(linea.cantidad()), Element.ALIGN_RIGHT, fondo));
            tabla.addCell(celdaLinea(formatoCop(linea.valorUnitario()), Element.ALIGN_RIGHT, fondo));
            tabla.addCell(celdaLinea(formatoCop(linea.subtotal()), Element.ALIGN_RIGHT, fondo));
            par = !par;
        }
        return tabla;
    }

    private static PdfPTable totales(FacturaResponse factura) throws DocumentException {
        PdfPTable tabla = new PdfPTable(new float[]{8, 3});
        tabla.setWidthPercentage(100);
        PdfPCell etiqueta = celdaSinBorde(new Phrase("TOTAL A PAGAR", TOTAL));
        etiqueta.setHorizontalAlignment(Element.ALIGN_RIGHT);
        etiqueta.setPaddingTop(10);
        tabla.addCell(etiqueta);
        PdfPCell valor = celdaSinBorde(new Phrase(formatoCop(factura.total()), TOTAL));
        valor.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valor.setPaddingTop(10);
        tabla.addCell(valor);
        return tabla;
    }

    private static PdfPCell celdaLinea(String texto, int alineacion, Color fondo) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, NORMAL));
        celda.setHorizontalAlignment(alineacion);
        celda.setBackgroundColor(fondo);
        celda.setBorderColor(GRIS_BORDE);
        celda.setPadding(6);
        return celda;
    }

    private static PdfPCell celdaSinBorde(Phrase contenido) {
        PdfPCell celda = contenido == null ? new PdfPCell() : new PdfPCell(contenido);
        celda.setBorder(Rectangle.NO_BORDER);
        return celda;
    }

    private static Paragraph alineado(Paragraph parrafo) {
        parrafo.setAlignment(Element.ALIGN_RIGHT);
        return parrafo;
    }

    /** Pesos colombianos: "$ 1.234.567" (sin decimales si son cero; con ",50" si los hay). */
    static String formatoCop(BigDecimal valor) {
        DecimalFormatSymbols simbolos = DecimalFormatSymbols.getInstance(COLOMBIA);
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        DecimalFormat formato = new DecimalFormat("#,##0.##", simbolos);
        formato.setMinimumFractionDigits(valor.stripTrailingZeros().scale() > 0 ? 2 : 0);
        return "$ " + formato.format(valor);
    }
}
