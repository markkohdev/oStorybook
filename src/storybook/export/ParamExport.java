/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package storybook.export;

import storybook.SbConstants;
import storybook.toolkit.DocumentUtil;
import storybook.ui.MainFrame;

/**
 *
 * @author favdb
 */
public class ParamExport {

	MainFrame mainFrame;
	boolean csvSingleQuotes, csvDoubleQuotes, csvNoQuotes;
	boolean csvComma;
	boolean txtTab;
	String txtSeparator;
	boolean htmlUseCss;
	String htmlCssFile;
	boolean isExportChapterNumbers;
	boolean isExportChapterNumbersRoman;
	boolean isExportChapterTitles;
	boolean isExportChapterDatesLocs;
	boolean isExportSceneTitles;
	boolean isExportSceneSeparator;
	boolean htmlBookMulti;
	String pdfPageSize;
	boolean pdfLandscape;

	ParamExport(MainFrame m) {
		mainFrame = m;
	}

	void load() {
		String x = DocumentUtil
				.restoreInternal(mainFrame, SbConstants.InternalKey.CSV_QUOTES, "010").getStringValue();
		csvSingleQuotes = false;
		csvDoubleQuotes = true;
		csvNoQuotes = false;
		if (!"".equals(x)) {
			if ("1".equals(x.substring(0, 1)))
				csvSingleQuotes = true;
			else if ("1".equals(x.substring(1, 2)))
				csvDoubleQuotes = true;
			else if ("1".equals(x.substring(2)))
				csvNoQuotes = true;
		}
		csvComma = DocumentUtil
				.restoreInternal(mainFrame, SbConstants.InternalKey.CSV_COMMA, false).getBooleanValue();
		txtTab = DocumentUtil
				.restoreInternal(mainFrame, SbConstants.InternalKey.TXT_TAB, true).getBooleanValue();
		if (!txtTab)
			txtSeparator = DocumentUtil
					.restoreInternal(mainFrame, SbConstants.InternalKey.TXT_OTHER, "").getStringValue();
		else
			txtSeparator = "";
		htmlUseCss = DocumentUtil
				.restoreInternal(mainFrame, SbConstants.InternalKey.HTML_USE_CSS, false).getBooleanValue();
		if (htmlUseCss)
			htmlCssFile = DocumentUtil
					.restoreInternal(mainFrame, SbConstants.InternalKey.HTML_CSS_FILE, "").getStringValue();
		else
			htmlCssFile = "";
		htmlBookMulti = DocumentUtil
				.restoreInternal(mainFrame, SbConstants.InternalKey.HTML_BOOK_MULTI, false).getBooleanValue();
		isExportChapterNumbers = DocumentUtil
				.restoreInternal(mainFrame, SbConstants.InternalKey.EXPORT_CHAPTER_NUMBERS, false).getBooleanValue();
		isExportChapterNumbersRoman = DocumentUtil
				.restoreInternal(mainFrame, SbConstants.InternalKey.EXPORT_ROMAN_NUMERALS, false).getBooleanValue();
		isExportChapterTitles = DocumentUtil
				.restoreInternal(mainFrame, SbConstants.InternalKey.EXPORT_CHAPTER_TITLES, false).getBooleanValue();
		isExportChapterDatesLocs = DocumentUtil
				.restoreInternal(mainFrame, SbConstants.InternalKey.EXPORT_CHAPTER_DATES_LOCATIONS, false).getBooleanValue();
		isExportSceneTitles = DocumentUtil
				.restoreInternal(mainFrame, SbConstants.InternalKey.EXPORT_SCENE_TITLES, false).getBooleanValue();
		isExportSceneSeparator = DocumentUtil
				.restoreInternal(mainFrame, SbConstants.InternalKey.EXPORT_SCENE_SEPARATOR, false).getBooleanValue();
		pdfPageSize = DocumentUtil
				.restoreInternal(mainFrame, SbConstants.InternalKey.PDF_PAGE_SIZE, "A4").getStringValue();
		pdfLandscape = DocumentUtil
				.restoreInternal(mainFrame, SbConstants.InternalKey.PDF_LANDSCAPE, false).getBooleanValue();
	}

	void save() {
		String x = (csvSingleQuotes ? "1" : "0") + (csvDoubleQuotes ? "1" : "0") + (csvNoQuotes ? "1" : "0");
		DocumentUtil.storeInternal(mainFrame, SbConstants.InternalKey.CSV_QUOTES.toString(), x);
		DocumentUtil.storeInternal(mainFrame, SbConstants.InternalKey.CSV_COMMA.toString(), csvComma);
		DocumentUtil.storeInternal(mainFrame, SbConstants.InternalKey.TXT_TAB.toString(), txtTab);
		DocumentUtil.storeInternal(mainFrame, SbConstants.InternalKey.TXT_OTHER.toString(), txtSeparator);
		DocumentUtil.storeInternal(mainFrame, SbConstants.InternalKey.HTML_USE_CSS.toString(), htmlUseCss);
		DocumentUtil.storeInternal(mainFrame, SbConstants.InternalKey.HTML_CSS_FILE.toString(), htmlCssFile);
		DocumentUtil.storeInternal(mainFrame, SbConstants.InternalKey.HTML_BOOK_MULTI.toString(), htmlBookMulti);
		DocumentUtil.storeInternal(mainFrame, SbConstants.InternalKey.EXPORT_CHAPTER_NUMBERS.toString(), isExportChapterNumbers);
		DocumentUtil.storeInternal(mainFrame, SbConstants.InternalKey.EXPORT_ROMAN_NUMERALS.toString(), isExportChapterNumbersRoman);
		DocumentUtil.storeInternal(mainFrame, SbConstants.InternalKey.EXPORT_CHAPTER_TITLES.toString(), isExportChapterTitles);
		DocumentUtil.storeInternal(mainFrame, SbConstants.InternalKey.EXPORT_CHAPTER_DATES_LOCATIONS.toString(), isExportChapterDatesLocs);
		DocumentUtil.storeInternal(mainFrame, SbConstants.InternalKey.EXPORT_SCENE_TITLES.toString(), isExportSceneTitles);
		DocumentUtil.storeInternal(mainFrame, SbConstants.InternalKey.EXPORT_SCENE_SEPARATOR.toString(), isExportSceneSeparator);
		DocumentUtil.storeInternal(mainFrame, SbConstants.InternalKey.PDF_PAGE_SIZE.toString(), pdfPageSize);
		DocumentUtil.storeInternal(mainFrame, SbConstants.InternalKey.PDF_LANDSCAPE.toString(), pdfLandscape);
	}
	
}
