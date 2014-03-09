/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package storybook.export;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;
import storybook.StorybookApp;

/**
 *
 * @author favdb
 */
public class ExportPDF {

	private Export parent;
	private Document outDoc;
	String report;
	String fileName = "";
	List<ExportHeader> headers;
	Font fontHeader, fontBody;
	FileOutputStream fop;
	PdfPTable table;
	String author;

	public ExportPDF(Export parent, String report, String fileName, List<ExportHeader> headers, String author) {
		this.parent = parent;
		this.report = report;
		this.fileName = fileName;
		this.headers = headers;
		this.author = author;
	}

	public void writeRow(String[] strings) {
		StorybookApp.trace("ExportPDF.writeRow()");
		for (String str : strings) {
			table.addCell(str);
		}
	}

	private void addMetaData() {
		StorybookApp.trace("ExportPDF.addMetaData()");
		outDoc.addTitle(report);
		outDoc.addSubject("Base list");
		outDoc.addKeywords("oStoryBook");
		outDoc.addAuthor(author);
		outDoc.addCreator(System.getProperty("user.name"));
	}

	public void open() {
		StorybookApp.trace("ExportPDF.open()");
		outDoc = new Document();
		Rectangle rectangle=new Rectangle(PageSize.getRectangle(parent.parent.paramExport.pdfPageSize));
		if (parent.parent.paramExport.pdfLandscape) {
			rectangle=new Rectangle(PageSize.getRectangle(parent.parent.paramExport.pdfPageSize).rotate());
		}
		outDoc.setPageSize(rectangle);
		try {
			PdfWriter.getInstance(outDoc, new FileOutputStream(fileName));
		} catch (FileNotFoundException | DocumentException ex) {
			StorybookApp.error(ExportPDF.class.getName(), ex);
		}
		outDoc.open();

		addMetaData();
		try {
			outDoc.add(new Phrase(parent.bookTitle+" - "+parent.exportData.getKey()+"\n"
					, FontFactory.getFont(FontFactory.HELVETICA, 14, Font.BOLD)));
		} catch (DocumentException ex) {
			StorybookApp.error("ExportPDF.open()", ex);
		}
		if (headers == null)
			return;
		float hsize[] = new float[headers.size()];
		int i = 0;
		for (ExportHeader header : headers) {
			hsize[i] = header.getSize();
			i++;
		}

		table = new PdfPTable(hsize);

		for (ExportHeader header : headers) {
			table.addCell(new Phrase(header.getName(), FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD)));
		}
	}

	void writeText(String str) {
		StorybookApp.trace("ExportPDF.writeText("+str+")");
		try {
			outDoc.add(new Phrase(str+"\n", FontFactory.getFont(FontFactory.HELVETICA, 10)));
		} catch (DocumentException ex) {
			StorybookApp.error("ExportPDF.writeText(" + str + ")", ex);
		}
	}

	public void close() {
		StorybookApp.trace("ExportPDF.close()");
		try {
			if (headers != null)
				outDoc.add(table);
		} catch (DocumentException ex) {
			StorybookApp.error("ExportPDF.close()", ex);
		}
		outDoc.close();
	}

}
