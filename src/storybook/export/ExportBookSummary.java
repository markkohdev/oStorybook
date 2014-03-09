/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package storybook.export;

import storybook.SbConstants;
import storybook.toolkit.DocumentUtil;
import storybook.toolkit.I18N;

/**
 *
 * @author favdb
 */
public class ExportBookSummary {

	private final Export parent;
	private ExportHtml html;
	private ExportPDF pdf;
	private ExportCsv csv;
	private ExportTxt txt;
	private ExportOdf odf;

	ExportBookSummary(Export m) {
		parent = m;
	}

	public String get() {
		switch (parent.format) {
			case "html":
				html=new ExportHtml(parent,"BookSummary",parent.file.getAbsolutePath(),null,parent.author);
				break;
			case "csv":
				csv=new ExportCsv(parent,"BookSummary",parent.file.getAbsolutePath(),null,parent.author);
				break;
			case "txt":
				txt=new ExportTxt(parent,"BookSummary",parent.file.getAbsolutePath(),null,parent.author);
				break;
			case "pdf":
				pdf=new ExportPDF(parent,"BookSummary",parent.file.getAbsolutePath(),null,parent.author);
				break;
			case "odf":
				odf=new ExportOdf(parent,"BookSummary",parent.file.getAbsolutePath(),null,parent.author);
				break;
		}
		String str = debut();
		ligne(I18N.getMsgColon("msg.common.title"),
				DocumentUtil.restoreInternal(parent.mainFrame, SbConstants.InternalKey.TITLE, "").getStringValue());
		ligne(I18N.getMsgColon("msg.common.subtitle"),
				DocumentUtil.restoreInternal(parent.mainFrame, SbConstants.InternalKey.SUBTITLE, "").getStringValue());
		ligne(I18N.getMsgColon("msg.common.author_s"),
				DocumentUtil.restoreInternal(parent.mainFrame, SbConstants.InternalKey.AUTHOR, "").getStringValue());
		ligne(I18N.getMsgColon("msg.common.copyright"),
				DocumentUtil.restoreInternal(parent.mainFrame, SbConstants.InternalKey.COPYRIGHT, "").getStringValue());
		ligne(I18N.getMsgColon("msg.common.notes"),
				DocumentUtil.restoreInternal(parent.mainFrame, SbConstants.InternalKey.NOTES, "").getStringValue());
		ligne(I18N.getMsgColon("msg.common.blurb"),
				DocumentUtil.restoreInternal(parent.mainFrame, SbConstants.InternalKey.BLURB, "").getStringValue());
		fin();
		return (str);
	}

	public String debut() {
		String str = "";
		switch (parent.format) {
			case "html":
				html.open();
				break;
			case "csv":
				csv.open();
				break;//no header
			case "txt":
				txt.open();
				break;//no header
			case "pdf":
				pdf.open();
				break;
			case "odf":
				odf.open();
				break;
		}
		return (str);
	}

	private void ligne(String lib, String data) {
		String str=lib+" "+data;
		switch (parent.format) {
			case "html":
				str = "<b>"+lib+"</b> "+data;
				html.writeText(str);
				break;
			case "csv":
				csv.writeText(str);
				break;
			case "txt":
				txt.writeText(str);
				break;
			case "pdf":
				pdf.writeText(str);
				break;
			case "odf":
				odf.writeText(str);
				break;
		}
	}

	private void fin() {
		switch(parent.format) {
			case "html":
				html.close();
				break;
			case "pdf":
				pdf.close();
				break;
			case "csv":
				csv.close();
				break;
			case "txt":
				txt.close();
				break;
		}
	}
}
