/*
Storybook: Open Source software for novelists and authors.
Copyright (C) 2008 - 2012 Martin Mustun

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package storybook.ui.dialog;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Calendar;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.BevelBorder;

import net.miginfocom.swing.MigLayout;

import org.hibernate.Session;
import storybook.SbConstants.BookKey;
import storybook.model.BookModel;
import storybook.model.hbn.dao.ChapterDAOImpl;
import storybook.model.hbn.dao.ItemDAOImpl;
import storybook.model.hbn.dao.LocationDAOImpl;
import storybook.model.hbn.dao.PartDAOImpl;
import storybook.model.hbn.dao.PersonDAOImpl;
import storybook.model.hbn.dao.SceneDAOImpl;
import storybook.model.hbn.dao.TagDAOImpl;
import storybook.model.hbn.entity.Internal;
import storybook.model.hbn.entity.Scene;
import storybook.toolkit.DateUtil;
import storybook.toolkit.BookUtil;
import storybook.toolkit.I18N;
import storybook.toolkit.TextUtil;
import storybook.toolkit.html.HtmlSelection;
import storybook.toolkit.html.HtmlUtil;
import storybook.toolkit.swing.SwingUtil;
import storybook.ui.MainFrame;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class BookPropertiesDialog extends AbstractDialog {

	private JTabbedPane tabbedPane;

	private JCheckBox cbUseLibreOffice;
	private JCheckBox cbUseHtmlScenes;
	private JCheckBox cbUseHtmlDescr;
	private JCheckBox cbEditorFullToolbar;
	private JCheckBox cbExportChapterNumbers;
	private JCheckBox cbExportRomanNumerals;
	private JCheckBox cbExportChapterTitles;
	private JCheckBox cbExportChapterDatesLocations;
	private JCheckBox cbExportSceneTitles;
	private JCheckBox cbExportPartTitles;

	private JTextField tfTitle;
	private JTextField tfSubtitle;
	private JTextField tfAuthor;
	private JTextField tfCopyright;
	private JTextArea taBlurb;
	private JTextArea taNotes;

	private JTextPane tpInfo;

	public BookPropertiesDialog(MainFrame mainFrame) {
		super(mainFrame);
		initAll();
	}

	@Override
	public void init() {
	}

	@Override
	public void initUi() {
		super.initUi();
		setLayout(new MigLayout("wrap,fill", "", "[grow][]"));
		setTitle(I18N.getMsg("msg.document.preference.title"));
		Dimension dim = new Dimension(520, 460);
		setPreferredSize(dim);
		setMinimumSize(dim);

		tabbedPane = new JTabbedPane();
		tabbedPane.addTab(I18N.getMsg("msg.dlg.preference.global"), createGeneralTab());
		tabbedPane.addTab(I18N.getMsg("msg.common.properties"), createPropertyTab());
		tabbedPane.addTab(I18N.getMsg("msg.file.info"), createInfoTab());

		// layout
		add(tabbedPane, "grow");
		add(getOkButton(), "split 2,sg,right");
		add(getCancelButton(), "sg");
	}

	private JPanel createPropertyTab() {
		JPanel panel = new JPanel();
		MigLayout layout = new MigLayout("wrap 2", "[][grow]", "[][][grow][grow]");
		panel.setLayout(layout);

		JLabel lbTitle = new JLabel(I18N.getMsgColon("msg.common.title"));
		tfTitle = new JTextField();
		Internal internal = BookUtil.get(mainFrame, BookKey.TITLE, "");
		tfTitle.setText(internal.getStringValue());

		JLabel lbSubtitle = new JLabel(I18N.getMsgColon("msg.common.subtitle"));
		tfSubtitle = new JTextField();
		internal = BookUtil.get(mainFrame, BookKey.SUBTITLE, "");
		tfSubtitle.setText(internal.getStringValue());

		JLabel lbAuthor = new JLabel(I18N.getMsgColon("msg.common.author_s"));
		tfAuthor = new JTextField();
		internal = BookUtil.get(mainFrame, BookKey.AUTHOR, "");
		tfAuthor.setText(internal.getStringValue());

		JLabel lbCopyright = new JLabel(I18N.getMsgColon("msg.common.copyright"));
		tfCopyright = new JTextField();
		internal = BookUtil.get(mainFrame, BookKey.COPYRIGHT, "");
		tfCopyright.setText(internal.getStringValue());

		JLabel lbNotes = new JLabel(I18N.getMsgColon("msg.common.notes"));
		taNotes = new JTextArea();
		taNotes.setLineWrap(true);
		taNotes.setWrapStyleWord(true);
		taNotes.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		internal = BookUtil.get(mainFrame, BookKey.NOTES, "");
		taNotes.setText(internal.getStringValue());
		taNotes.setCaretPosition(0);

		JLabel lbBlurb = new JLabel(I18N.getMsgColon("msg.common.blurb"));
		taBlurb = new JTextArea();
		taBlurb.setLineWrap(true);
		taBlurb.setWrapStyleWord(true);
		taBlurb.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		internal = BookUtil.get(mainFrame, BookKey.BLURB, "");
		taBlurb.setText(internal.getStringValue());
		taBlurb.setCaretPosition(0);

		// layout
		panel.add(lbTitle);
		panel.add(tfTitle, "growx");

		panel.add(lbSubtitle);
		panel.add(tfSubtitle, "growx");

		panel.add(lbAuthor);
		panel.add(tfAuthor, "growx");

		panel.add(lbCopyright);
		panel.add(tfCopyright, "growx");

		panel.add(lbBlurb, "top");
		JScrollPane scroller = new JScrollPane(taBlurb);
		SwingUtil.setMaxPreferredSize(scroller);
		panel.add(scroller, "grow");

		panel.add(lbNotes, "top");
		scroller = new JScrollPane(taNotes);
		SwingUtil.setMaxPreferredSize(scroller);
		panel.add(scroller, "grow");

		return panel;
	}

	private JPanel createInfoTab() {
		JPanel panel = new JPanel();
		MigLayout layout = new MigLayout("wrap,fill", "[]", "[grow][]");
		panel.setLayout(layout);

		BookModel model = mainFrame.getBookModel();
		Session session = model.beginTransaction();

		int textLength = 0;
		int words = 0;
		SceneDAOImpl sceneDao = new SceneDAOImpl(session);
		List<Scene> scenes = sceneDao.findAll();
		for (Scene scene : scenes) {
			String text = HtmlUtil.htmlToText(scene.getText());
			textLength += text.length();
			words += TextUtil.countWords(text);
		}

		File file = mainFrame.getDbFile().getFile();

		PartDAOImpl partDao = new PartDAOImpl(session);
		ChapterDAOImpl chapterDao = new ChapterDAOImpl(session);
		PersonDAOImpl personDao = new PersonDAOImpl(session);
		LocationDAOImpl locationDao = new LocationDAOImpl(session);
		TagDAOImpl tagDao = new TagDAOImpl(session);
		ItemDAOImpl itemDao = new ItemDAOImpl(session);

		StringBuilder buf = new StringBuilder();
		buf.append("<html>");
		buf.append(HtmlUtil.getHeadWithCSS());
		buf.append("<body><table>");
		buf.append(HtmlUtil.getRow2Cols(I18N.getMsgColon("msg.file.info.filename"), file.toString()));
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(file.lastModified());
		buf.append(HtmlUtil.getRow2Cols(I18N.getMsgColon("msg.file.info.last.mod"), DateUtil.calendarToString(cal)));
		buf.append(HtmlUtil.getRow2Cols( I18N.getMsgColon("msg.file.info.text.length"), Integer.toString(textLength)));
		buf.append(HtmlUtil.getRow2Cols(I18N.getMsgColon("msg.file.info.words"), Integer.toString(words)));
		buf.append(HtmlUtil.getRow2Cols(I18N.getMsgColon("msg.common.parts"), Integer.toString(partDao.count(null))));
		buf.append(HtmlUtil.getRow2Cols(I18N.getMsgColon("msg.common.chapters"), Integer.toString(chapterDao.count(null))));
		buf.append(HtmlUtil.getRow2Cols(I18N.getMsgColon("msg.common.scenes"), Integer.toString(sceneDao.count(null))));
		buf.append(HtmlUtil.getRow2Cols(I18N.getMsgColon("msg.common.persons"), Integer.toString(personDao.count(null))));
		buf.append(HtmlUtil.getRow2Cols(I18N.getMsgColon("msg.menu.locations"), Integer.toString(locationDao.count(null))));
		buf.append(HtmlUtil.getRow2Cols(I18N.getMsgColon("msg.tags"), Integer.toString(tagDao.count(null))));
		buf.append(HtmlUtil.getRow2Cols(I18N.getMsgColon("msg.items"), Integer.toString(itemDao.count(null))));
		buf.append("</table></body></html>");

		model.commit();

		tpInfo = new JTextPane();
		tpInfo.setContentType("text/html");
		tpInfo.setEditable(false);
		tpInfo.setMinimumSize(new Dimension(400, 300));
		tpInfo.setText(buf.toString());
		tpInfo.setBorder(SwingUtil.getBorderEtched());
		JPopupMenu popup = new JPopupMenu();
		SwingUtil.addCopyToPopupMenu(popup, tpInfo);
		tpInfo.setComponentPopupMenu(popup);

		// copy text
		JButton btCopyText = new JButton();
		btCopyText.setAction(getCopyTextAction());
		btCopyText.setText(I18N.getMsg("msg.file.info.copy.text"));
		btCopyText.setIcon(I18N.getIcon("icon.small.copy"));

		// layout
		panel.add(tpInfo, "grow");
		panel.add(btCopyText, "sg,left,span");

		return panel;
	}

	private JPanel createGeneralTab() {
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("wrap 2"));

		cbUseLibreOffice = new JCheckBox();
		cbUseLibreOffice.setText(I18N.getMsg("msg.document.preference.use.libreoffice"));
		cbUseLibreOffice.setSelected(BookUtil.isUseLibreOffice(mainFrame));

		cbUseHtmlScenes = new JCheckBox();
		cbUseHtmlScenes.setText(I18N.getMsg("msg.document.preference.use.html.scenes"));
		cbUseHtmlScenes.setSelected(BookUtil.isUseHtmlScenes(mainFrame));

		cbUseHtmlDescr = new JCheckBox();
		cbUseHtmlDescr.setText(I18N.getMsg("msg.document.preference.use.html.descr"));
		cbUseHtmlDescr.setSelected(BookUtil.isUseHtmlDescr(mainFrame));

		cbEditorFullToolbar = new JCheckBox();
		cbEditorFullToolbar.setText(I18N.getMsg("msg.document.preference.editor.full.toolbar"));
		cbEditorFullToolbar.setSelected(BookUtil.isEditorFullToolbar(mainFrame));

		cbExportChapterNumbers = new JCheckBox();
		cbExportChapterNumbers.setText(I18N.getMsg("msg.export.chapter.numbers"));
		cbExportChapterNumbers.setSelected(BookUtil.isExportChapterNumbers(mainFrame));

		cbExportRomanNumerals = new JCheckBox();
		cbExportRomanNumerals.setText(I18N.getMsg("msg.export.roman.numerals"));
		cbExportRomanNumerals.setSelected(BookUtil.isExportRomanNumerals(mainFrame));

		cbExportChapterTitles = new JCheckBox();
		cbExportChapterTitles.setText(I18N.getMsg("msg.export.chapter.titles"));
		cbExportChapterTitles.setSelected(BookUtil.isExportChapterTitles(mainFrame));

		cbExportChapterDatesLocations = new JCheckBox();
		cbExportChapterDatesLocations.setText(I18N.getMsg("msg.export.chapter.dates.locations"));
		cbExportChapterDatesLocations.setSelected(BookUtil.isExportChapterDatesLocations(mainFrame));

		cbExportSceneTitles = new JCheckBox();
		cbExportSceneTitles.setText(I18N.getMsg("msg.export.scene.titles"));
		cbExportSceneTitles.setSelected(BookUtil.isExportSceneTitle(mainFrame));

		cbExportPartTitles = new JCheckBox();
		cbExportPartTitles.setText(I18N.getMsg("msg.export.part.titles"));
		cbExportPartTitles.setSelected(BookUtil.isExportPartTitles(mainFrame));

		// layout
		addTitle(panel, "msg.document.preference.formatted.title");
		panel.add(new JLabel());
		panel.add(cbUseLibreOffice);
		panel.add(new JLabel());
		panel.add(cbUseHtmlScenes);
		panel.add(new JLabel());
		panel.add(cbUseHtmlDescr);
		panel.add(new JLabel());
		panel.add(cbEditorFullToolbar);

		addTitle(panel, "msg.export.settings");
		panel.add(new JLabel());
		panel.add(cbExportChapterNumbers);
		panel.add(new JLabel());
		panel.add(cbExportRomanNumerals);
		panel.add(new JLabel());
		panel.add(cbExportChapterTitles);
		panel.add(new JLabel());
		panel.add(cbExportChapterDatesLocations);
		panel.add(new JLabel());
		panel.add(cbExportSceneTitles);
		panel.add(new JLabel());
		panel.add(cbExportPartTitles);

		return panel;
	}

	private void addTitle(JPanel panel, String i18nKey) {
		JLabel lb = new JLabel(I18N.getMsg(i18nKey));
		lb.setFont(SwingUtil.getFontBold(12));
		panel.add(lb, "span,gaptop 10");
	}

	@Override
	protected AbstractAction getOkAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// general settings
				BookUtil.store(mainFrame, BookKey.USE_LIBREOFFICE, cbUseLibreOffice.isSelected());
				BookUtil.store(mainFrame, BookKey.USE_HTML_SCENES, cbUseHtmlScenes.isSelected());
				BookUtil.store(mainFrame, BookKey.USE_HTML_DESCR, cbUseHtmlDescr.isSelected());
				BookUtil.store(mainFrame, BookKey.EDITOR_FULL_TOOLBAR, cbEditorFullToolbar.isSelected());
				BookUtil.store(mainFrame, BookKey.EXPORT_CHAPTER_NUMBERS, cbExportChapterNumbers.isSelected());
				BookUtil.store(mainFrame, BookKey.EXPORT_ROMAN_NUMERALS, cbExportRomanNumerals.isSelected());
				BookUtil.store(mainFrame, BookKey.EXPORT_CHAPTER_TITLES, cbExportChapterTitles.isSelected());
				BookUtil.store(mainFrame, BookKey.EXPORT_CHAPTER_DATES_LOCATIONS, cbExportChapterDatesLocations.isSelected());
				BookUtil.store(mainFrame, BookKey.EXPORT_SCENE_TITLES, cbExportSceneTitles.isSelected());
				BookUtil.store(mainFrame, BookKey.EXPORT_PART_TITLES, cbExportPartTitles.isSelected());
				// properties
				BookUtil.store(mainFrame, BookKey.TITLE, tfTitle.getText());
				BookUtil.store(mainFrame, BookKey.SUBTITLE, tfSubtitle.getText());
				BookUtil.store(mainFrame, BookKey.AUTHOR, tfAuthor.getText());
				BookUtil.store(mainFrame, BookKey.COPYRIGHT, tfCopyright.getText());
				BookUtil.store(mainFrame, BookKey.BLURB, taBlurb.getText());
				BookUtil.store(mainFrame, BookKey.NOTES, taNotes.getText());
				canceled = false;
				dispose();
				mainFrame.setTitle();
				mainFrame.getBookController().fireAgain();
			}
		};
	}

	private AbstractAction getCopyTextAction() {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				HtmlSelection selection = new HtmlSelection(tpInfo.getText());
				Clipboard clbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
				clbrd.setContents(selection, selection);
			}
		};
	}
}
