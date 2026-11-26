package com.xochitepec.patrimonio.traspaso;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.xochitepec.patrimonio.parametro.ParametroRepository;
import com.xochitepec.patrimonio.parametro.ParametroSistema;

import lombok.RequiredArgsConstructor;

/**
 * Genera el "Vale de traspaso de bienes con responsabilidad de resguardo" a partir de los datos
 * congelados (snapshot) de una solicitud autorizada. Se arma al vuelo; no se guardan archivos.
 */
@Service
@RequiredArgsConstructor
public class ValeTraspasoPdfService {

    private static final String TEXTO_LEGAL = "POR MEDIO DEL PRESENTE VALE DE TRASPASO DE BIENES CON RESPONSABILIDAD "
            + "DE RESGUARDO, EL BIEN (ES) ENTREGADO (S) POR EL SERVIDOR (A) PÚBLICO QUE ENTREGA SE DA (N) DE BAJA "
            + "EL (LOS) BIEN (ES) ELIMINÁNDOSE SU INSCRIPCIÓN EN EL INVENTARIO GENERAL CORRESPONDIENTE AL ÁREA "
            + "ANTERIOR; ASÍ MISMO EL SERVIDOR (A) PÚBLICO QUE RECIBE EL BIEN (ES) SE COMPROMETE A DAR BUEN USO Y "
            + "AL MISMO TIEMPO, ACEPTA TODA RESPONSABILIDAD DE CUALQUIER DAÑO O EXTRAVÍO QUE SUFRA (N) EL (LOS) "
            + "BIEN (ES) QUE A CONTINUACIÓN SE DESCRIBE (N):";

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color GRIS = new Color(230, 230, 230);

    private static final Font F_TITULO = new Font(Font.HELVETICA, 11, Font.BOLD);
    private static final Font F_NEGRITA = new Font(Font.HELVETICA, 8.5f, Font.BOLD);
    private static final Font F_NORMAL = new Font(Font.HELVETICA, 8.5f, Font.NORMAL);
    private static final Font F_CHICA = new Font(Font.HELVETICA, 7.5f, Font.NORMAL);
    private static final Font F_CHICA_NEGRITA = new Font(Font.HELVETICA, 7.5f, Font.BOLD);

    private final ParametroRepository parametros;

    public byte[] generar(SolicitudTraspaso s) {
        Map<String, String> p = parametros.findAll().stream()
                .collect(Collectors.toMap(ParametroSistema::getClave, x -> Objects.toString(x.getValor(), "")));
        List<SolicitudTraspasoDetalle> detalles = s.getDetalles();
        LocalDate fecha = s.getFechaResolucion().atZone(TraspasoService.ZONA).toLocalDate();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.LETTER, 36, 36, 30, 30);
        PdfWriter.getInstance(doc, out);
        doc.open();

        doc.add(encabezado(s, fecha));

        Paragraph titulo = new Paragraph("VALE DE TRASPASO DE BIENES CON RESPONSABILIDAD DE RESGUARDO", F_TITULO);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingBefore(8);
        titulo.setSpacingAfter(6);
        doc.add(titulo);

        Paragraph para = new Paragraph();
        para.add(new Phrase("TRASPASO DE BIEN PARA:  ", F_NEGRITA));
        para.add(new Phrase(s.getAreaDestino().getNombre().toUpperCase(Locale.ROOT), F_NORMAL));
        para.add(new Phrase("     (DE: " + s.getAreaOrigen().getNombre().toUpperCase(Locale.ROOT) + ")", F_CHICA));
        para.setSpacingAfter(6);
        doc.add(para);

        Paragraph legal = new Paragraph(TEXTO_LEGAL, F_CHICA);
        legal.setAlignment(Element.ALIGN_JUSTIFIED);
        legal.setSpacingAfter(8);
        doc.add(legal);

        doc.add(datosCompra(detalles));
        doc.add(tablaBienes(detalles));
        doc.add(firmas(s, detalles, p));

        doc.close();
        return out.toByteArray();
    }

    private PdfPTable encabezado(SolicitudTraspaso s, LocalDate fecha) {
        PdfPTable t = new PdfPTable(new float[] {1.2f, 2f});
        t.setWidthPercentage(100);

        PdfPCell logo = sinBorde(new PdfPCell());
        Image img = logo();
        if (img != null) {
            img.scaleToFit(130, 60);
            logo.addElement(img);
        }
        t.addCell(logo);

        PdfPCell datos = sinBorde(new PdfPCell());
        datos.setHorizontalAlignment(Element.ALIGN_RIGHT);
        datos.addElement(derecha("FOLIO: ", String.format("%03d", s.getFolio()) + " / " + s.getAnioFolio()));
        datos.addElement(derecha("FECHA: ", fecha.format(FECHA)));
        datos.addElement(derecha("OFICIO DE PETICIÓN NÚMERO: ", Objects.toString(s.getOficioPeticion(), "S/N")));
        t.addCell(datos);
        return t;
    }

    private PdfPTable datosCompra(List<SolicitudTraspasoDetalle> d) {
        PdfPTable t = new PdfPTable(new float[] {1.6f, 1.2f, 1f, 1.4f, 1.1f, 1.4f, 1.2f, 1f});
        t.setWidthPercentage(100);
        t.setSpacingAfter(6);
        t.addCell(etiqueta("DOCUMENTO SOPORTE PARA ALTA:"));
        t.addCell(valor(unicos(d, SolicitudTraspasoDetalle::getSnapDocumentoSoporte)));
        t.addCell(etiqueta("NO. DE FACTURA:"));
        t.addCell(valor(unicos(d, SolicitudTraspasoDetalle::getSnapNoFactura)));
        t.addCell(etiqueta("EXPEDIDA POR:"));
        t.addCell(valor(unicos(d, SolicitudTraspasoDetalle::getSnapExpedidaPor)));
        t.addCell(etiqueta("FECHA DE FACTURA:"));
        t.addCell(valor(unicos(d, x -> x.getSnapFechaFactura() == null ? null : x.getSnapFechaFactura().format(FECHA))));
        return t;
    }

    private PdfPTable tablaBienes(List<SolicitudTraspasoDetalle> detalles) {
        PdfPTable t = new PdfPTable(new float[] {0.5f, 2.2f, 1.6f, 2f, 1.4f, 1.3f, 0.9f, 1.1f});
        t.setWidthPercentage(100);
        t.setHeaderRows(1);
        for (String h : new String[] {"NO", "DESCRIPCIÓN", "MARCA Y MODELO", "CARACTERÍSTICAS", "NO. DE SERIE",
                "NO. DE INVENTARIO", "ESTADO", "COSTO DEL BIEN"}) {
            PdfPCell c = new PdfPCell(new Phrase(h, F_CHICA_NEGRITA));
            c.setBackgroundColor(GRIS);
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setVerticalAlignment(Element.ALIGN_MIDDLE);
            c.setPadding(4);
            t.addCell(c);
        }
        int n = 1;
        for (SolicitudTraspasoDetalle d : detalles) {
            t.addCell(celda(String.format("%02d", n++), Element.ALIGN_CENTER));
            t.addCell(celda(d.getSnapDescripcion(), Element.ALIGN_LEFT));
            t.addCell(celda(unir(d.getSnapMarca(), d.getSnapModelo()), Element.ALIGN_LEFT));
            t.addCell(celda(d.getSnapEspecificaciones(), Element.ALIGN_LEFT));
            t.addCell(celda(d.getSnapSerie(), Element.ALIGN_CENTER));
            t.addCell(celda(d.getSnapNoInventario(), Element.ALIGN_CENTER));
            t.addCell(celda(d.getSnapEstado(), Element.ALIGN_CENTER));
            t.addCell(celda(moneda(d.getSnapCosto()), Element.ALIGN_RIGHT));
        }
        return t;
    }

    private PdfPTable firmas(SolicitudTraspaso s, List<SolicitudTraspasoDetalle> d, Map<String, String> p) {
        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(100);
        t.setSpacingBefore(28);
        t.addCell(firma("AUTORIZA", p.get("firma.patrimonio.nombre"), p.get("firma.patrimonio.cargo")));
        t.addCell(firma("Vo. Bo.", p.get("firma.contabilidad.nombre"), p.get("firma.contabilidad.cargo")));
        t.addCell(firma("ENTREGA EN CONDICIONES FUNCIONALES",
                unicos(d, SolicitudTraspasoDetalle::getSnapEntregaNombre),
                unir(unicos(d, SolicitudTraspasoDetalle::getSnapEntregaCargo), s.getAreaOrigen().getNombre())));
        t.addCell(firma("RECIBE EN CONDICIONES FUNCIONALES", s.getRecibeNombre(),
                unir(s.getRecibeCargo(), s.getAreaDestino().getNombre())));
        return t;
    }

    private PdfPCell firma(String rotulo, String nombre, String cargo) {
        PdfPCell c = sinBorde(new PdfPCell());
        c.setPaddingBottom(30);
        Paragraph r = new Paragraph(rotulo, F_NEGRITA);
        r.setAlignment(Element.ALIGN_CENTER);
        r.setSpacingAfter(34);
        c.addElement(r);
        Paragraph linea = new Paragraph("_______________________________________", F_NORMAL);
        linea.setAlignment(Element.ALIGN_CENTER);
        c.addElement(linea);
        Paragraph n = new Paragraph(vacioSiNulo(nombre).toUpperCase(Locale.ROOT), F_NEGRITA);
        n.setAlignment(Element.ALIGN_CENTER);
        c.addElement(n);
        Paragraph cg = new Paragraph(vacioSiNulo(cargo).toUpperCase(Locale.ROOT), F_CHICA);
        cg.setAlignment(Element.ALIGN_CENTER);
        c.addElement(cg);
        return c;
    }

    private static Paragraph derecha(String etiqueta, String valor) {
        Paragraph p = new Paragraph();
        p.add(new Phrase(etiqueta, F_NEGRITA));
        p.add(new Phrase(valor, F_NORMAL));
        p.setAlignment(Element.ALIGN_RIGHT);
        return p;
    }

    private static PdfPCell etiqueta(String texto) {
        PdfPCell c = new PdfPCell(new Phrase(texto, F_CHICA_NEGRITA));
        c.setBackgroundColor(GRIS);
        c.setPadding(4);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return c;
    }

    private static PdfPCell valor(String texto) {
        PdfPCell c = new PdfPCell(new Phrase(vacioSiNulo(texto, "SIN DATO"), F_CHICA));
        c.setPadding(4);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return c;
    }

    private static PdfPCell celda(String texto, int alineacion) {
        PdfPCell c = new PdfPCell(new Phrase(vacioSiNulo(texto), F_CHICA));
        c.setHorizontalAlignment(alineacion);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPadding(4);
        return c;
    }

    private static PdfPCell sinBorde(PdfPCell c) {
        c.setBorder(Rectangle.NO_BORDER);
        return c;
    }

    /** Valores distintos de un campo entre todos los bienes, separados por coma. */
    private static String unicos(List<SolicitudTraspasoDetalle> d, Function<SolicitudTraspasoDetalle, String> f) {
        String r = d.stream().map(f).filter(x -> x != null && !x.isBlank()).distinct()
                .collect(Collectors.joining(", "));
        return r.isEmpty() ? null : r;
    }

    private static String unir(String a, String b) {
        if (a == null || a.isBlank()) {
            return b;
        }
        if (b == null || b.isBlank()) {
            return a;
        }
        return a + " - " + b;
    }

    private static String moneda(BigDecimal v) {
        if (v == null) {
            return "";
        }
        return new DecimalFormat("$#,##0.00", DecimalFormatSymbols.getInstance(Locale.US)).format(v);
    }

    private static String vacioSiNulo(String s) {
        return vacioSiNulo(s, "");
    }

    private static String vacioSiNulo(String s, String defecto) {
        return s == null || s.isBlank() ? defecto : s;
    }

    private static Image logo() {
        try (InputStream in = new ClassPathResource("vale/logo_xochi.png").getInputStream()) {
            return Image.getInstance(in.readAllBytes());
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo cargar el logo del vale", e);
        }
    }
}
