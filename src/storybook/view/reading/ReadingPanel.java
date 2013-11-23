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

package storybook.view.reading;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import net.infonode.docking.View;
import net.miginfocom.swing.MigLayout;

import org.hibernate.Session;
import storybook.SbConstants;
import storybook.StorybookApp;
import storybook.SbConstants.InternalKey;
import storybook.SbConstants.ViewName;
import storybook.controller.DocumentController;
import storybook.export.BookExporter;
import storybook.model.DocumentModel;
import storybook.model.hbn.dao.ChapterDAOImpl;
import storybook.model.hbn.entity.Chapter;
import storybook.model.hbn.entity.Internal;
import storybook.model.hbn.entity.Part;
import storybook.toolkit.DocumentUtil;
import storybook.toolkit.I18N;
import storybook.toolkit.ViewUtil;
import storybook.toolkit.html.HtmlUtil;
import storybook.toolkit.swing.SwingUtil;
import storybook.view.AbstractPanel;
import storybook.view.MainFrame;
import storybook.view.options.ReadingOptionsDialog;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class ReadingPanel extends AbstractPanel implements HyperlinkListener {

	private JTextPane tpText;
	private JScrollPane scroller;
	private StrandPanel strandPanel;

	private int scrollerWidth;
	private int fontSize;

	public ReadingPanel(MainFrame mainFrame) {
		super(mainFrame);
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		String propName = evt.getPropertyName();
		Object newValue = evt.getNewValue();

		if (DocumentController.SceneProps.INIT.check(propName)) {
			super.refresh();
			return;
		}

		if (DocumentController.CommonProps.REFRESH.check(propName)) {
			View newView = (View) evt.getNewValue();
			View view = (View) getParent().getParent();
			if (view == newView) {
				// super.refresh();
				refresh();
			}
			return;
		}

		if (DocumentController.ChapterProps.INIT.check(propName)
				|| DocumentController.ChapterProps.UPDATE.check(propName)
				|| DocumentController.SceneProps.UPDATE.check(propName)) {
			refresh();
			return;
		}

		if (DocumentController.CommonProps.SHOW_OPTIONS.check(propName)) {
			View view = (View) evt.getNewValue();
			if (!view.getName().equals(ViewName.READING.toString())) {
				return;
			}
			ReadingOptionsDialog dlg = new ReadingOptionsDialog(mainFrame);
			SwingUtil.showModalDialog(dlg, this);
			return;
		}

		if (DocumentController.ReadingViewProps.ZOOM.check(propName)) {
			setZoomedSize((Integer) newValue);
			scroller.setMaximumSize(new Dimension(scrollerWidth, 10000));
			scroller.getParent().invalidate();
			scroller.getParent().validate();
			scroller.getParent().repaint();
			return;
		}

		if (DocumentController.ReadingViewProps.FONT_SIZE.check(propName)) {
			setFontSize((Integer) newValue);
			refresh();
			return;
		}

		if (DocumentController.PartProps.CHANGE.check(propName)) {
			ViewUtil.scrollToTop(scroller);
			// super.refresh();
			refresh();
			return;
		}


		dispatchToStrandPanels(this, evt);

		if (DocumentController.StrandProps.UPDATE.check(propName)) {
			refresh();
			return;
		}
	}

	private void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	private void setZoomedSize(int zoomValue) {
		scrollerWidth = zoomValue * 10;
	}

	@Override
	public void init() {
		strandPanel = new StrandPanel(mainFrame, this);
		strandPanel.init();

		try {
			Internal internal = DocumentUtil.restoreInternal(mainFrame,
					InternalKey.READING_ZOOM, SbConstants.DEFAULT_READING_ZOOM);
			setZoomedSize(internal.getIntegerValue());
			internal = DocumentUtil.restoreInternal(mainFrame,
					InternalKey.READING_FONT_SIZE,
					SbConstants.DEFAULT_READING_FONT_SIZE);
			setFontSize(internal.getIntegerValue());
		} catch (Exception e) {
			e.printStackTrace();
			setZoomedSize(SbConstants.DEFAULT_READING_ZOOM);
			setFontSize(SbConstants.DEFAULT_READING_FONT_SIZE);
		}
	}

	@Override
	public void initUi() {
		MigLayout layout = new MigLayout(
				"flowx",
				"[][fill,grow]", // columns
				"" // rows
				);
		setLayout(layout);

		strandPanel.initUi();

		tpText = new JTextPane();
		/* SB5 suppress Pro
		if (!StorybookApp.getInstance().isProVersion()) {
			tpText.setHighlighter(null);
		}
		*/
		tpText.setEditable(false);
		tpText.setContentType("text/html");
		tpText.addHyperlinkListener(this);

		scroller = new JScrollPane(tpText);
		scroller.setMaximumSize(new Dimension(scrollerWidth, Short.MAX_VALUE));
		SwingUtil.setMaxPreferredSize(scroller);

		// layout
		add(strandPanel, "aligny top");
		add(scroller, "growy");
	}

	@Override
	public void refresh() {
		Part currentPart = mainFrame.getCurrentPart();
		boolean isUsehtmlText = DocumentUtil.isUseHtmlScenes(mainFrame);
		boolean expPartTitles = DocumentUtil.isExportPartTitles(mainFrame);

		DocumentModel model = mainFrame.getDocumentModel();
		Session session = model.beginTransaction();
		ChapterDAOImpl dao = new ChapterDAOImpl(session);
		List<Chapter> chapters = dao
				.findAllOrderByChapterNoAndSceneNo(currentPart);
		model.commit();

		StringBuffer buf = new StringBuffer();
		buf.append(HtmlUtil.getHeadWithCSS(fontSize));

		// table of contents
		buf.append("<p style='font-weight:bold'>");
		buf.append("<a name='toc'>" + I18N.getMsg("msg.table.of.contents")
				+ "</a></p>\n");
		for (Chapter chapter : chapters) {
			String no = chapter.getChapternoStr();
			buf.append("<p><a href='#" + no + "'>");
			buf.append(no + ": " + chapter.getTitle() + "</a>");
			String descr = chapter.getDescription();
			if (descr != null) {
				if (!descr.isEmpty()) {
					buf.append(": " + chapter.getDescription());
				}
			}
			buf.append("</p>\n");
		}

		if(!expPartTitles && !isUsehtmlText){
			buf.append("<p></p>\n");
		}

		// content
		BookExporter exp = new BookExporter(mainFrame);
		exp.setExportOnlyCurrentPart(true);
		exp.setExportTableOfContentsLink(true);
		exp.setStrandIdsToExport(strandPanel.getStrandIds());
		buf.append(exp.getContent());

		// old
//		for (Chapter chapter : chapters) {
//			session = model.beginTransaction();
//			dao = new ChapterDAOImpl(session);
//			List<Scene> scenes = dao.findScenes(chapter);
//			model.commit();
//			buf.append("<h2><a name='" + chapter.getChapternoStr() + "'>");
//			String no = chapter.getChapternoStr();
//			buf.append(no + ": " + chapter.getTitle());
//			buf.append("</a></h2>\n");
//			for (Scene scene : scenes) {
//				long strandId = scene.getStrand().getId();
//				if (!strandPanel.getStrandIds().contains(strandId)) {
//					continue;
//				}
//				buf.append("<p style='margin-top:10px;margin-bottom:4px;'>");
//				buf.append("<span style='background-color:");
//				Strand strand = scene.getStrand();
//				String clr = strand.getHTMLColor();
//				buf.append(clr + ";'>&nbsp;&nbsp;&nbsp;&nbsp;</span>&nbsp;");
//				buf.append("<span><b>" + scene.getTitle() + "</b></span> \n");
//				buf.append("</p>\n");
//				buf.append("<p>\n");
//				if (DocumentUtil.isUseHtmlScenes(mainFrame)) {
//					buf.append(scene.getText());
//				} else {
//					buf.append(HtmlUtil.textToHTML(scene.getText()));
//				}
//				buf.append("</p>");
//			}
//			buf.append("<p style='font-size:8px;text-align:left;'><a href='#toc'>"
//					+ I18N.getMsg("msg.table.of.contents") + "</a></p>");
//		}

		buf.append("<p>&nbsp;</body></html>\n");

		final int pos = scroller.getVerticalScrollBar().getValue();
		tpText.setText(buf.toString());
		final Action restoreAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				scroller.getVerticalScrollBar().setValue(pos);
			}
		};
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				restoreAction.actionPerformed(null);
			}
		});
	}

	@Override
	public void hyperlinkUpdate(HyperlinkEvent evt) {
		if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			try {
				if (!evt.getDescription().isEmpty()) {
					// anchor
					tpText.scrollToReference(evt.getDescription().substring(1));
				} else {
					// external links
					tpText.setPage(evt.getURL());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void dispatchToStrandPanels(Container cont,
			PropertyChangeEvent evt) {
		List<Component> ret = new ArrayList<Component>();
		SwingUtil.findComponentsByClass(cont, StrandPanel.class, ret);
		for (Component comp : ret) {
			StrandPanel panel = (StrandPanel) comp;
			panel.modelPropertyChange(evt);
		}
	}
}
