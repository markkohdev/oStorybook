/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package storybook.export;

import storybook.model.DbFile;
import storybook.model.DocumentModel;
import storybook.model.hbn.dao.ChapterDAOImpl;
import storybook.model.hbn.dao.PartDAOImpl;
import storybook.model.hbn.dao.SceneDAOImpl;
import storybook.model.hbn.entity.Chapter;
import storybook.model.hbn.entity.Part;
import storybook.model.hbn.entity.Scene;
import storybook.model.hbn.entity.Strand;
import storybook.toolkit.DateUtil;
import storybook.toolkit.DocumentUtil;
import storybook.toolkit.I18N;
import storybook.toolkit.LangUtil;
import storybook.toolkit.html.HtmlSelection;
import storybook.toolkit.html.HtmlUtil;
import storybook.view.MainFrame;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;

/**
 *
 * @author favdb
 */
public class BookExporter extends AbstractExporter {

	private boolean exportForOpenOffice = false;
	private boolean exportOnlyCurrentPart = false;
	private boolean exportTableOfContentsLink = false;
	private HashSet<Long> strandIdsToExport = null;
	private String bH1="<h1>",eH1="</h1>\n\n",
		bH2="<h2>",eH2="</h2>\n",
		bH3="<h3>",eH3="</h3>\n",
		bH4="<h4>", eH4="</h4\n";

	public BookExporter(MainFrame m) {
		super(m);
		setFileName(m.getDbFile().getName());
	}

	public boolean exportToClipboard() {
		try {
			StringBuffer localStringBuffer = getContent();
			HtmlSelection localHtmlSelection = new HtmlSelection(localStringBuffer.toString());
			Clipboard localClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			localClipboard.setContents(localHtmlSelection, localHtmlSelection);
			JOptionPane.showMessageDialog(this.mainFrame, I18N.getMsg("msg.book.copy.confirmation"), I18N.getMsg("msg.copied.title"), 1);
		} catch (Exception IOException) {
			return false;
		}
		return true;
	}

	@Override
	public StringBuffer getContent() {
		Part Part1 = this.mainFrame.getCurrentPart();
		StringBuffer buf = new StringBuffer();
		try {
			boolean isUseHtmlScenes = DocumentUtil.isUseHtmlScenes(this.mainFrame);
			boolean isExportChapterNumbers = DocumentUtil.isExportChapterNumbers(this.mainFrame);
			boolean isExportRomanNumerals = DocumentUtil.isExportRomanNumerals(this.mainFrame);
			boolean isExportChapterTitles = DocumentUtil.isExportChapterTitles(this.mainFrame);
			boolean isExportChapterDatesLocations = DocumentUtil.isExportChapterDatesLocations(this.mainFrame);
			boolean isExportSceneTitle = DocumentUtil.isExportSceneTitle(this.mainFrame);
			boolean isExportPartTitles = DocumentUtil.isExportPartTitles(this.mainFrame);
			int i = 1;
			if ((!isUseHtmlScenes) && (this.exportForOpenOffice == true)) {
				i = 0;
				bH1="";eH1="\n\n";
				bH2="";eH2="\n";
				bH3="";eH3="\n";
				bH4="";eH4="\n";
			} else {
				bH1="<h1>";eH1="</h1>\n";
				bH2="<h2>";eH2="</h2>\n";
				bH3="<h3>";eH3="</h3>\n";
				bH4="<h4>";eH4="</h4\n";
			}
			if ((isUseHtmlScenes) && (this.exportForOpenOffice == true)) {
				buf
					.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n")
					.append("<html>\n")
					.append("<head>\n")
					.append("<title>")
					.append(this.mainFrame.getDbFile().getName())
					.append("</title>\n")
					.append("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'/>\n")
					.append("</head>\n")
					.append("<body>\n");
			}
			DocumentModel model = this.mainFrame.getDocumentModel();
			Session session = model.beginTransaction();
			PartDAOImpl PartDAO = new PartDAOImpl(session);
			ChapterDAOImpl ChapterDAO = new ChapterDAOImpl(session);
			SceneDAOImpl SceneDAO = new SceneDAOImpl(session);
			Object Object1;
			if (this.exportOnlyCurrentPart) {
				Object1 = new ArrayList();
				((List) Object1).add(Part1);
			} else {
				Object1 = PartDAO.findAll();
			}
			Iterator Iterator1 = ((List) Object1).iterator();
			while (Iterator1.hasNext()) {
				Part Part2 = (Part) Iterator1.next();
				if (isExportPartTitles) {
					buf.append(bH1);
					buf.append(I18N.getMsg("msg.common.part"));
					buf.append(": ");
					buf.append(Part2.getNumber());
					buf.append(eH1);
				}
				List localList = ChapterDAO.findAll(Part2);
				Iterator Iterator2 = localList.iterator();
				while (Iterator2.hasNext()) {
					Chapter localChapter = (Chapter) Iterator2.next();
					buf.append(bH2);
					if (i != 0) {
						buf.append("<a name='").append(localChapter.getChapternoStr()).append("'>");
					} else {
						buf.append("\n");
					}
					if (isExportChapterNumbers) {
						if (isExportRomanNumerals) {
							buf.append((String) LangUtil.intToRoman(localChapter.getChapterno().intValue()));
						} else {
							buf.append(localChapter.getChapternoStr());
						}
					}
					if (isExportChapterTitles) {
						buf.append(": ");
						buf.append(localChapter.getTitle());
					}
					if (i != 0) {
						buf.append("</a>\n");
					}
					buf.append(eH2);
					if (isExportChapterDatesLocations) {
						buf.append(bH3);
						buf.append(DateUtil.getNiceDates((List) ChapterDAO.findDates(localChapter)));
						if (!((List) ChapterDAO.findLocations(localChapter)).isEmpty()) {
							buf.append(": ");
							buf.append(StringUtils.join((Iterable) ChapterDAO.findLocations(localChapter), ", "));
						}
						buf.append(eH3);
					}
					Object localObject2 = SceneDAO.findByChapter(localChapter);
					Object localObject3 = ((List) localObject2).iterator();
					boolean bx;
					while (((Iterator) localObject3).hasNext()) {
						Scene localScene = (Scene) ((Iterator) localObject3).next();
						bx = true;
						if (this.strandIdsToExport != null) {
							long l = localScene.getStrand().getId().longValue();
							if (!this.strandIdsToExport.contains(Long.valueOf(l))) {
								bx = false;
							}
						}
						if (bx) {
							if (!localScene.getInformative().booleanValue()) {
								if (isExportSceneTitle) {
									buf.append(bH4);
									buf.append(localScene.getTitle());
									buf.append(eH4);
								}
								String str = localScene.getText();
								if (isUseHtmlScenes) {
									HtmlUtil.appendCleanHtml(buf, str);
								} else {
									buf.append(str);
									if (!this.exportForOpenOffice) {
										buf.append("<br>");
									}
								}
								buf.append("\n");
							}
						}
					}
					if (this.exportTableOfContentsLink) {
						buf.append("<p style='font-size:8px;text-align:left;'><a href='#toc'>")
							.append(I18N.getMsg("msg.table.of.contents"))
							.append("</a></p>");
					}
				}
			}
			model.commit();
			if ((isUseHtmlScenes) && (this.exportForOpenOffice == true)) {
				buf.append("</body>\n")
					.append("</html>\n")
					.append("\n");
			}
		} catch (Exception exception) {
		}
		return buf;
	}

	public boolean isExportOnlyCurrentPart() {
		return this.exportOnlyCurrentPart;
	}

	public void setExportOnlyCurrentPart(boolean b) {
		this.exportOnlyCurrentPart = b;
	}

	public boolean isExportTableOfContentsLink() {
		return this.exportTableOfContentsLink;
	}

	public void setExportTableOfContentsLink(boolean b) {
		this.exportTableOfContentsLink = b;
	}

	public HashSet<Long> getStrandIdsToExport() {
		return this.strandIdsToExport;
	}

	public void setStrandIdsToExport(HashSet<Long> p) {
		this.strandIdsToExport = p;
	}

	public boolean isExportForOpenOffice() {
		return this.exportForOpenOffice;
	}

	public void setExportForOpenOffice(boolean b) {
		this.exportForOpenOffice = b;
	}
}