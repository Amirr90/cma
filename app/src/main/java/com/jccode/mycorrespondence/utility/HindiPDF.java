package com.jccode.mycorrespondence.utility;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class HindiPDF {


    public static final String DEST = "PDF/0.pdf";
    public static final String FONT = "resources/fonts/freesans.ttf";

    public HindiPDF() throws IOException, DocumentException {
        File file = new File(DEST);
        file.getParentFile().mkdirs();
        createPdf(DEST);
    }

    public void createPdf(String dest) throws IOException, DocumentException {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(dest));
        document.open();
        Font f = FontFactory.getFont(FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Paragraph p1 = new Paragraph("\u0915\u093e\u0930 \u092a\u093e\u0930\u094d\u0915\u093f\u0902\u0917", f);
        document.add(p1);
        Paragraph p2 = new Paragraph("\\u0915 \u0915 \\u093e \u093e \\0930 \u0930\n"
                + "\\u092a \u092a \\u093e \u093e \\u0930 \u0930 \\u094d \u094d"
                + "\\u0915 \u0915 \\u093f \\u093f \u093f \\u0902 \u0902"
                + "\\u0917 \u0917", f);
        document.add(p2);

        BaseFont unicode = BaseFont.createFont(FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font font = new Font(unicode, 12, Font.NORMAL, new BaseColor(50, 205, 50));
        PdfPTable table = new PdfPTable(new float[]{10, 60, 30});
        table.setWidthPercentage(100);
        PdfPCell customerLblCell = new PdfPCell(new Phrase("CUSTOMERS"));
        PdfPCell balanceLblCell = new PdfPCell(new Phrase("\u0915\u093e\u0930\u092a\u093e\u0930\u094d\u0915\u093f\u0902\u0917", font));
        table.addCell(customerLblCell);
        table.addCell(balanceLblCell);
        table.completeRow();
        table.setSpacingBefore(10);
        document.add(table);
        document.close();
    }
}
