/*
 * SbApp: Open Source software for novelists and authors.
 * Original idea 2008 - 2012 Martin Mustun
 * Copyrigth (C) Favdb
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package storybook.ui.jasperreports;

import storybook.model.BookModel;
import storybook.toolkit.I18N;
import storybook.toolkit.swing.SwingUtil;
import storybook.ui.MainFrame;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.JRTextExporterParameter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.view.JasperViewer;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import storybook.StorybookApp;

public class ExportManager implements ReportConstants {

	private final MainFrame mainFrame;
	private final List<ExportReport> reports;
	private static JasperPrint jasperPrint;
	private final HashMap<String, String> formatMap;

	public ExportManager(MainFrame param) {
		mainFrame = param;
		reports = new ArrayList();
		reports.add(new ExportReport("person_list", "msg.jasper.person.list"));
		reports.add(new ExportReport("location_list", "msg.jasper.location.list"));
		reports.add(new ExportReport("tag_list", "msg.jasper.tag.list"));
		reports.add(new ExportReport("item_list", "msg.jasper.item.list"));
		reports.add(new ExportReport("idea_list", "msg.jasper.idea.list"));
		reports.add(new ExportReport("summary", "msg.jasper.book.summary"));
		reports.add(new ExportReport("csv_person_list", "msg.jasper.csv.person.list"));
		reports.add(new ExportReport("csv_location_list", "msg.jasper.csv.location.list"));
		reports.add(new ExportReport("csv_tag_list", "msg.jasper.csv.tag.list"));
		reports.add(new ExportReport("csv_item_list", "msg.jasper.csv.item.list"));
		reports.add(new ExportReport("csv_idea_list", "msg.jasper.csv.idea.list"));
		reports.add(new ExportReport("csv_summary", "msg.jasper.csv.book.summary"));
		formatMap = new HashMap();
		formatMap.put("pdf", "PDF");
		formatMap.put("html", "HTML");
		formatMap.put("csv", "CSV (comma-separated values)");
		formatMap.put("text", "Text (UTF-8)");
		formatMap.put("rtf", "RTF (Rich Text Format)");
		formatMap.put("odt", "ODT (OpenDocument Text)");
	}

	public List<ExportReport> getReportList() {
		return reports;
	}

	public HashMap<String, String> getFormatMap() {
		return formatMap;
	}

	public void fillReport(final ExportReport param) {
		BookModel model = this.mainFrame.getDocumentModel();
		Session session = model.beginTransaction();
		session.doWork(new Work() {
			@Override
			public void execute(Connection connection) throws SQLException {
				try {
					String str = "jasperreports" + File.separator + param.getJasperReportName() + ".jasper";
					HashMap localHashMap = new HashMap();
					localHashMap.put("PARAM_TITLE", param.getName() + " - " + I18N.getMsg("msg.common.project")
							+ ": \"" +mainFrame.getDbFile().getName() + "\"");
					localHashMap.put("SUBREPORT_DIR", "jasperreports" + File.separator);
					/* TODO solve access$102
					 ExportManager.access$102(JasperFillManager.fillReport(str, localHashMap, connection));
					 */
					JasperFillManager.fillReport(str, localHashMap, connection);
					/* TODO solve access$102 end */
					System.out.println("fillReport()");
				} catch (JRException e) {
					StorybookApp.error("ExportManager.fillReport("+param.getName()+")", e);
				}
			}
		});
		model.commit();
	}

	public JasperPrint getJasperPrint() {
		return jasperPrint;
	}

	public String export(File file, String type, ExportReport exportReport) {
		try {
			String str = "";
			if (!"preview".equals(type)) file.mkdirs();
			fillReport(exportReport);
			switch (type) {
				case "pdf":
					str = getExportFileName(file, exportReport.getName(), "pdf");
					JasperExportManager.exportReportToPdfFile(jasperPrint, str);
					break;
				case "preview":
					JasperViewer.viewReport(jasperPrint, false);
					break;
				case "html":
					str = getExportFileName(file, exportReport.getName(), "html");
					JasperExportManager.exportReportToHtmlFile(jasperPrint, str);
					break;
				default:
					switch (type) {
						case "csv":
							str = getExportFileName(file, exportReport.getName(), "csv");
							JRCsvExporter objCsv = new JRCsvExporter();
							objCsv.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
							objCsv.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, str);
							objCsv.exportReport();
							break;
						case "text":
							str = getExportFileName(file, exportReport.getName(), "txt");
							JRTextExporter objTxt = new JRTextExporter();
							objTxt.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
							objTxt.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, str);
							objTxt.setParameter(JRTextExporterParameter.PAGE_WIDTH, new Integer(80));
							objTxt.setParameter(JRTextExporterParameter.PAGE_HEIGHT, new Integer(100));
							objTxt.exportReport();
							break;
						case "rtf":
							str = getExportFileName(file, exportReport.getName(), "rtf");
							JRRtfExporter objRtf = new JRRtfExporter();
							objRtf.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
							objRtf.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, str);
							objRtf.exportReport();
							break;
						case "odt":
							str = getExportFileName(file, exportReport.getName(), "odt");
							JROdtExporter objOdt = new JROdtExporter();
							objOdt.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
							objOdt.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, str);
							objOdt.exportReport();
							break;
					}
					break;
			}
			return str;
		} catch (JRException localException) {
		}
		return "";
	}

	private String getExportFileName(File file, String str1, String str2) {
		StringBuilder str = new StringBuilder();
		str.append(file);
		str.append(File.separator);
		str.append(str1);
		str.append("_");
		str.append(SwingUtil.getTimestamp(new Date()));
		str.append(".");
		str.append(str2);
		return str.toString();
	}
}
