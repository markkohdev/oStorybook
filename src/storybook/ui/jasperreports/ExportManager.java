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

import storybook.model.DbFile;
import storybook.model.DocumentModel;
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
import java.util.Map;
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

public class ExportManager implements ReportConstants {

	private MainFrame mainFrame;
	private List<ExportReport> reports;
	private static JasperPrint jasperPrint;
	private HashMap<String, String> formatMap;

	public ExportManager(MainFrame mainframe) {
		mainFrame = mainframe;
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
		DocumentModel model = this.mainFrame.getDocumentModel();
		Session session = model.beginTransaction();
		session.doWork(new Work() {
			@Override
			public void execute(Connection connection)
				throws SQLException {
				try {
					String str = "resources/reports"
						+ File.separator
						+ param.getJasperReportName()
						+ ".jasper";
					HashMap localHashMap = new HashMap();
					localHashMap.put("PARAM_TITLE",
						param.getName()
						+ " - " + I18N.getMsg("msg.common.project")
						+ ": \"" + ExportManager.this.mainFrame.getDbFile().getName() + "\"");
					localHashMap.put("SUBREPORT_DIR", "resources/reports" + File.separator);
					/* TODO solve access$102
					 ExportManager.access$102(JasperFillManager.fillReport(str, localHashMap, connection));
					 */
					JasperFillManager.fillReport(str, localHashMap, connection);
					/* TODO solve access$102 end */
				} catch (JRException e) {
					System.err.println("ExportManager.fillReport(param)" + e.getMessage());
				}
			}
		});
		model.commit();
	}

	public JasperPrint getJasperPrint() {
		return jasperPrint;
	}

	public String export(File paramFile, String paramString, ExportReport paramExportReport) {
		try {
			String str = "";
			if (!"preview".equals(paramString)) {
				paramFile.mkdirs();
			}
			fillReport(paramExportReport);
			switch (paramString) {
				case "pdf":
					str = getExportFileName(paramFile, paramExportReport.getName(), "pdf");
					JasperExportManager.exportReportToPdfFile(jasperPrint, str);
					break;
				case "preview":
					JasperViewer.viewReport(jasperPrint, false);
					break;
				case "html":
					str = getExportFileName(paramFile, paramExportReport.getName(), "html");
					JasperExportManager.exportReportToHtmlFile(jasperPrint, str);
					break;
				default:
					Object localObject;
					switch (paramString) {
						case "csv":
							str = getExportFileName(paramFile, paramExportReport.getName(), "csv");
							localObject = new JRCsvExporter();
							((JRCsvExporter) localObject).setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
							((JRCsvExporter) localObject).setParameter(JRExporterParameter.OUTPUT_FILE_NAME, str);
							((JRCsvExporter) localObject).exportReport();
							break;
						case "text":
							str = getExportFileName(paramFile, paramExportReport.getName(), "txt");
							localObject = new JRTextExporter();
							((JRTextExporter) localObject).setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
							((JRTextExporter) localObject).setParameter(JRExporterParameter.OUTPUT_FILE_NAME, str);
							((JRTextExporter) localObject).setParameter(JRTextExporterParameter.PAGE_WIDTH, new Integer(80));
							((JRTextExporter) localObject).setParameter(JRTextExporterParameter.PAGE_HEIGHT, new Integer(100));
							((JRTextExporter) localObject).exportReport();
							break;
						case "rtf":
							str = getExportFileName(paramFile, paramExportReport.getName(), "rtf");
							localObject = new JRRtfExporter();
							((JRRtfExporter) localObject).setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
							((JRRtfExporter) localObject).setParameter(JRExporterParameter.OUTPUT_FILE_NAME, str);
							((JRRtfExporter) localObject).exportReport();
							break;
						case "odt":
							str = getExportFileName(paramFile, paramExportReport.getName(), "odt");
							localObject = new JROdtExporter();
							((JROdtExporter) localObject).setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
							((JROdtExporter) localObject).setParameter(JRExporterParameter.OUTPUT_FILE_NAME, str);
							((JROdtExporter) localObject).exportReport();
							break;
					}
					break;
			}
			return str;
		} catch (Exception localException) {
		}
		return "";
	}

	private String getExportFileName(File paramFile, String paramString1, String paramString2) {
		StringBuilder localStringBuffer = new StringBuilder();
		localStringBuffer.append(paramFile);
		localStringBuffer.append(File.separator);
		localStringBuffer.append(paramString1);
		localStringBuffer.append("_");
		localStringBuffer.append(SwingUtil.getTimestamp(new Date()));
		localStringBuffer.append(".");
		localStringBuffer.append(paramString2);
		return localStringBuffer.toString();
	}
}