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
/* vérification OK */

package storybook.action;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import net.infonode.docking.View;

import org.apache.commons.io.FileUtils;
import org.hibernate.Session;
import storybook.SbConstants;
import storybook.StorybookApp;
import storybook.SbConstants.InternalKey;
import storybook.SbConstants.ViewName;
import storybook.controller.DocumentController;
import storybook.model.DbFile;
import storybook.model.DocumentModel;
import storybook.model.EntityUtil;
import storybook.model.hbn.dao.PartDAOImpl;
import storybook.model.hbn.entity.AbstractEntity;
import storybook.model.hbn.entity.Category;
import storybook.model.hbn.entity.Chapter;
import storybook.model.hbn.entity.Gender;
import storybook.model.hbn.entity.Idea;
import storybook.model.hbn.entity.Internal;
import storybook.model.hbn.entity.Item;
import storybook.model.hbn.entity.ItemLink;
import storybook.model.hbn.entity.Location;
import storybook.model.hbn.entity.Part;
import storybook.model.hbn.entity.Person;
import storybook.model.hbn.entity.Scene;
import storybook.model.hbn.entity.Strand;
import storybook.model.hbn.entity.Tag;
import storybook.model.hbn.entity.TagLink;
import storybook.toolkit.DockingWindowUtil;
import storybook.toolkit.DocumentUtil;
import storybook.toolkit.EnvUtil;
import storybook.toolkit.I18N;
import storybook.toolkit.net.NetUtil;
import storybook.toolkit.net.Updater;
import storybook.toolkit.swing.SwingUtil;
import storybook.view.MainFrame;
import storybook.view.dialog.AboutDialog;
import storybook.view.dialog.CreateChaptersDialog;
import storybook.view.dialog.DocumentPreferencesDialog;
import storybook.view.dialog.FoiDialog;
import storybook.view.dialog.ManageLayoutsDialog;
import storybook.view.dialog.PreferencesDialog;
/* SB5 suppress Pro
 import storybook.view.dialog.ProOnlyDialog;
 */
import storybook.view.dialog.WaitDialog;
import storybook.view.dialog.file.RenameFileDialog;
import storybook.view.dialog.file.SaveAsFileDialog;
import storybook.view.dialog.rename.RenameCityDialog;
import storybook.view.dialog.rename.RenameCountryDialog;
import storybook.view.dialog.rename.RenameItemCategoryDialog;
import storybook.view.dialog.rename.RenameTagCategoryDialog;
import storybook.view.jasperreports.ExportPrintDialog;

import com.sun.jaf.ui.ActionManager;
import storybook.export.BookExporter;
import storybook.view.SbView;

/**
 * @author martin
 *
 */
public class ActionHandler {

	private MainFrame mainFrame;

	public ActionHandler(MainFrame mainframe) {
		mainFrame = mainframe;
	}

	public void handleCheckUpdate() {
		if (Updater.checkForUpdate()) {
			JOptionPane.showMessageDialog(mainFrame,
				I18N.getMsg("msg.update.no.text"),
				I18N.getMsg("msg.update.no.title"),
				JOptionPane.INFORMATION_MESSAGE);
		}
	}

	public void handleOpenExportFolder() {
		try {
			Internal internal = DocumentUtil.restoreInternal(mainFrame,
				InternalKey.EXPORT_DIRECTORY,
				EnvUtil.getDefaultExportDir(mainFrame));
			Desktop.getDesktop().open(new File(internal.getStringValue()));
		} catch (Exception ex) {
			System.err.println("ActionHandler.handleExportDir() Exception : " + ex.getMessage());
		} catch (Error er) {
			System.err.println("ActionHandler.handleExportDir() Error : " + er.getMessage());
		}
	}

	public void handleText2Html() {
		int n = SwingUtil.showBetaDialog(mainFrame);
		if (n == JOptionPane.NO_OPTION || n == JOptionPane.CLOSED_OPTION) {
			return;
		}
		mainFrame.setWaitingCursor();
		EntityUtil.convertPlainTextToHtml(mainFrame);
		mainFrame.refresh();
		mainFrame.setDefaultCursor();
	}

	public void handleHtml2Text() {
		int n = SwingUtil.showBetaDialog(mainFrame);
		if (n == JOptionPane.NO_OPTION || n == JOptionPane.CLOSED_OPTION) {
			return;
		}
		mainFrame.setWaitingCursor();
		EntityUtil.convertHtmlToPlainText(mainFrame);
		mainFrame.refresh();
		mainFrame.setDefaultCursor();
	}

	public void handleLangTool() {
		LangToolAction act = new LangToolAction(mainFrame);
		act.actionPerformed(null);
	}

	public void handleCreateChapters() {
		CreateChaptersDialog dlg = new CreateChaptersDialog(mainFrame);
		SwingUtil.showModalDialog(dlg, mainFrame);
	}

	public void handleRenameCity() {
		RenameCityDialog dlg = new RenameCityDialog(mainFrame);
		ActionManager actMngr = mainFrame.getSbActionManager()
			.getActionManager();
		Action act = actMngr.getAction("rename-city-command");
		Object obj = act.getValue(SbConstants.ActionKey.CATEGORY.toString());
		if (obj != null) {
			dlg.setSelectedItem(obj);
		}
		SwingUtil.showModalDialog(dlg, mainFrame);
		act.putValue(SbConstants.ActionKey.CATEGORY.toString(), null);
	}

	public void handleRenameCountry() {
		RenameCountryDialog dlg = new RenameCountryDialog(mainFrame);
		ActionManager actMngr = mainFrame.getSbActionManager()
			.getActionManager();
		Action act = actMngr.getAction("rename-country-command");
		Object obj = act.getValue(SbConstants.ActionKey.CATEGORY.toString());
		if (obj != null) {
			dlg.setSelectedItem(obj);
		}
		SwingUtil.showModalDialog(dlg, mainFrame);
		act.putValue(SbConstants.ActionKey.CATEGORY.toString(), null);
	}

	public void handleRenameTagCategory() {
		RenameTagCategoryDialog dlg = new RenameTagCategoryDialog(mainFrame);
		ActionManager actMngr = mainFrame.getSbActionManager()
			.getActionManager();
		Action act = actMngr.getAction("rename-tag-category-command");
		Object obj = act.getValue(SbConstants.ActionKey.CATEGORY.toString());
		if (obj != null) {
			dlg.setSelectedItem(obj);
		}
		SwingUtil.showModalDialog(dlg, mainFrame);
		act.putValue(SbConstants.ActionKey.CATEGORY.toString(), null);
	}

	public void handleRenameItemCategory() {
		RenameItemCategoryDialog dlg = new RenameItemCategoryDialog(mainFrame);
		ActionManager actMngr = mainFrame.getSbActionManager()
			.getActionManager();
		Action act = actMngr.getAction("rename-item-category-command");
		Object obj = act.getValue(SbConstants.ActionKey.CATEGORY.toString());
		if (obj != null) {
			dlg.setSelectedItem(obj);
		}
		SwingUtil.showModalDialog(dlg, mainFrame);
		act.putValue(SbConstants.ActionKey.CATEGORY.toString(), null);
	}

	public void handleNewScene()		{handleNewEntity(new Scene());}

	public void handleNewChapter()	{handleNewEntity(new Chapter());}

	public void handleNewPart()		{handleNewEntity(new Part());}

	public void handleNewStrand()		{handleNewEntity(new Strand());}

	public void handleNewPerson()		{handleNewEntity(new Person());}

	public void handleNewCategory()	{handleNewEntity(new Category());}

	public void handleNewGender()		{handleNewEntity(new Gender());}

	public void handleNewLocation()	{handleNewEntity(new Location());}

	public void handleNewTag()			{handleNewEntity(new Tag());}

	public void handleNewTagLink()	{handleNewEntity(new TagLink());}

	public void handleNewItem()		{handleNewEntity(new Item());}

	public void handleNewItemLink()	{handleNewEntity(new ItemLink());}

	public void handleNewIdea()		{handleNewEntity(new Idea());}

	private void handleNewEntity(AbstractEntity entity) {
		DocumentController ctrl = mainFrame.getDocumentController();
		ctrl.setEntityToEdit(entity);
		mainFrame.showView(ViewName.EDITOR);
	}

	public void handleFlashOfInspiration() {
		FoiDialog dlg = new FoiDialog(mainFrame);
		SwingUtil.showModalDialog(dlg, mainFrame);
	}

	public void handleTaskList() {
		showAndFocus(ViewName.SCENES);
		mainFrame.getDocumentController().showTaskList();
	}

	public void handleExportPrint() {
		/* SB5 suppress Pro
		 if (StorybookApp.getInstance().isProVersion()) {
		 ExportPrintDialog dlg = new ExportPrintDialog(mainFrame);
		 SwingUtil.showDialog(dlg, mainFrame);
		 } else {
		 ProOnlyDialog dlg = new ProOnlyDialog(mainFrame);
		 SwingUtil.showModalDialog(dlg, mainFrame);
		 }
		 */
		// always considere Pro version is true
		ExportPrintDialog dlg = new ExportPrintDialog(mainFrame);
		SwingUtil.showDialog(dlg, mainFrame);
	}

	public void handleChartGantt() {
		showAndFocus(ViewName.CHART_GANTT);
	}

	public void handleChartOccurrenceOfLocations() {
		showAndFocus(ViewName.CHART_OCCURRENCE_OF_LOCATIONS);
	}

	public void handleChartOccurrenceOfPersons() {
		showAndFocus(ViewName.CHART_OCCURRENCE_OF_PERSONS);
	}

	public void handleChartStrandsByDate() {
		showAndFocus(ViewName.CHART_STRANDS_BY_DATE);
	}

	public void handleChartWiWW() {
		showAndFocus(ViewName.CHART_WiWW);
	}

	public void handleChartPersonsByDate() {
		showAndFocus(ViewName.CHART_PERSONS_BY_DATE);
	}

	public void handleChartPersonsByScene() {
		showAndFocus(ViewName.CHART_PERSONS_BY_SCENE);
	}

	public void handlePreviousPart() {
		Part currentPart = mainFrame.getCurrentPart();
		DocumentModel model = mainFrame.getDocumentModel();
		Session session = model.beginTransaction();
		PartDAOImpl dao = new PartDAOImpl(session);
		List<Part> parts = dao.findAll();
		int index = parts.indexOf(currentPart);
		if (index == 0) {
			// already first part
			return;
		}
		--index;
		handleChangePart(parts.get(index));
	}

	public void handleNextPart() {
		Part currentPart = mainFrame.getCurrentPart();
		DocumentModel model = mainFrame.getDocumentModel();
		Session session = model.beginTransaction();
		PartDAOImpl dao = new PartDAOImpl(session);
		List<Part> parts = dao.findAll();
		int index = parts.indexOf(currentPart);
		if (index == parts.size() - 1) {
			// already last part
			return;
		}
		++index;
		handleChangePart(parts.get(index));
	}

	public void handleChangePart(Part part) {
		mainFrame.setWaitingCursor();
		Part currentPart = mainFrame.getCurrentPart();
		if (currentPart.getId().equals(part.getId())) {
			// same part
			return;
		}
		mainFrame.setCurrentPart(part);
		mainFrame.setTitle();
		mainFrame.getDocumentController().changePart(part);
		mainFrame.setDefaultCursor();
	}

	public void handleShowChronoView() {
		showAndFocus(ViewName.CHRONO);
	}

	public void handleShowBookView() {
		showAndFocus(ViewName.BOOK);
	}

	public void handleShowManageView() {
		showAndFocus(ViewName.MANAGE);
	}

	public void handleShowReadingView() {
		showAndFocus(ViewName.READING);
	}

	public void handleShowMemoria() {
		showAndFocus(ViewName.MEMORIA);
	}

	public void handleShowEditor() {
		mainFrame.showEditor();
	}

	public void handleShowTree() {
		showAndFocus(ViewName.TREE);
	}

	public void handleShowInfo() {
		showAndFocus(ViewName.INFO);
	}

	public void handleShowNavigation() {
		showAndFocus(ViewName.NAVIGATION);
	}

	public void handleDumpAttachedViews() {
		DocumentController ctrl = mainFrame.getDocumentController();
		ctrl.printAttachedViews();
	}
	/* suppression du garbage collector
	 public void handleRunGC() {
	 SwingUtil.printMemoryUsage();
	 System.out.println("ActionHandler.handleRunGC(): running GC...");
	 StorybookApp.getInstance().runGC();
	 SwingUtil.printMemoryUsage();
	 }
	 */

	public void handleDummy() {
		try {
			System.out.println("ActionHandler.handleDummy(): ");
			SbView view = mainFrame.getView(SbConstants.ViewName.EDITOR);
			mainFrame.getViewFactory().unloadView(view);
		} catch (Exception ex) {
			System.err.println("ActionHandler.handleDummy() Exception : "+ex.getMessage());
		}
	}


	public void handleShowInternals() {
		showAndFocus(ViewName.INTERNALS);
	}

	public void handleShowScenes() {
		showAndFocus(ViewName.SCENES);
	}

	public void handleShowTags() {
		showAndFocus(ViewName.TAGS);
	}

	public void handleShowTagLinks() {
		showAndFocus(ViewName.TAGLINKS);
	}

	public void handleShowItems() {
		showAndFocus(ViewName.ITEMS);
	}

	public void handleShowItemLinks() {
		showAndFocus(ViewName.ITEMLINKS);
	}

	public void handleShowIdeas() {
		showAndFocus(ViewName.IDEAS);
	}

	public void handleShowStrands() {
		showAndFocus(ViewName.STRANDS);
	}

	public void handleShowCategories() {
		showAndFocus(ViewName.CATEGORIES);
	}

	public void handleShowGenders() {
		showAndFocus(ViewName.GENDERS);
	}

	public void handleShowPersons() {
		showAndFocus(ViewName.PERSONS);
	}

	public void handleShowLocations() {
		showAndFocus(ViewName.LOCATIONS);
	}

	public void handleShowChapters() {
		showAndFocus(ViewName.CHAPTERS);
	}

	public void handleShowParts() {
		showAndFocus(ViewName.PARTS);
	}

	private void showAndFocus(ViewName viewName) {
		View view = mainFrame.getView(viewName);
		view.restore();
		view.restoreFocus();
	}

	public void handleNewFile() {
		StorybookApp.getInstance().createNewFile();
	}

	public void handleOpenFile() {
		mainFrame.setWaitingCursor();
		StorybookApp.getInstance().openFile();
		mainFrame.setDefaultCursor();
	}

	public void handleRecentClear() {
		StorybookApp.getInstance().clearRecentFiles();
	}

	public void handleSave() {
		WaitDialog dlg = new WaitDialog(mainFrame,I18N.getMsg("msg.file.saving"));
		Timer timer = new Timer(500, new DisposeDialogAction(dlg));
		timer.setRepeats(false);
		timer.start();
		SwingUtil.showModalDialog(dlg, mainFrame);
	}

	public void handleSaveAs() {
		SaveAsFileDialog dlg = new SaveAsFileDialog(mainFrame);
		SwingUtil.showModalDialog(dlg, mainFrame);
		if (dlg.isCanceled()) {
			return;
		}
		File file = dlg.getFile();
		try {
			FileUtils.copyFile(mainFrame.getDbFile().getFile(), file);
		} catch (IOException ioe) {
			System.err.println("ActionHandler.handleSaveAs() IOex : "+ioe.getMessage());
		}
		DbFile dbFile = new DbFile(file);
		OpenFileAction act = new OpenFileAction("", dbFile);
		act.actionPerformed(null);
	}

	public void handleRenameFile() {
		RenameFileDialog dlg = new RenameFileDialog(mainFrame);
		SwingUtil.showModalDialog(dlg, mainFrame);
		if (dlg.isCanceled()) {
			return;
		}
		File file = dlg.getFile();
		StorybookApp.getInstance().renameFile(mainFrame, file);
	}

	public void handleClose() {
		mainFrame.close();
	}

	public void handleSaveLayout() {
		String name = JOptionPane.showInputDialog(mainFrame,
			I18N.getMsgColon("msg.common.enter.name"),
			I18N.getMsg("msg.docking.save.layout"),
			JOptionPane.PLAIN_MESSAGE);
		if (name != null) {
			DockingWindowUtil.saveLayout(mainFrame, name);
		}
	}

	public void handleManageLayouts() {
		ManageLayoutsDialog dlg = new ManageLayoutsDialog(mainFrame);
		SwingUtil.showModalDialog(dlg, mainFrame);
	}

	public void handleDefaultLayout() {
		DockingWindowUtil.setLayout(mainFrame,
			DockingWindowUtil.DEFAULT_LAYOUT);
	}

	public void handlePersonsLocationsLayout() {
		DockingWindowUtil.setLayout(mainFrame,
			DockingWindowUtil.PERSONS_LOCATIONS_LAYOUT);
	}

	public void handleTagsItemsLayout() {
		DockingWindowUtil.setLayout(mainFrame,
			DockingWindowUtil.TAGS_ITEMS_LAYOUT);
	}

	public void handleChronoOnlyLayout() {
		DockingWindowUtil.setLayout(mainFrame,
			DockingWindowUtil.CHRONO_ONLY_LAYOUT);
	}

	public void handleBookOnlyLayout() {
		DockingWindowUtil.setLayout(mainFrame,
			DockingWindowUtil.BOOK_ONLY_LAYOUT);
	}

	public void handleManageOnlyLayout() {
		DockingWindowUtil.setLayout(mainFrame,
			DockingWindowUtil.MANAGE_ONLY_LAYOUT);
	}

	public void handleReadingOnlyLayout() {
		DockingWindowUtil.setLayout(mainFrame,
			DockingWindowUtil.READING_ONLY_LAYOUT);
	}

	public void handleResetLayout() {
		SwingUtil.setWaitingCursor(mainFrame);
		mainFrame.setDefaultLayout();
		/* suppression du garbage collector
		 StorybookApp.getInstance().runGC();
		 */
		SwingUtil.setDefaultCursor(mainFrame);
	}

	public void handleRefresh() {
		mainFrame.refresh();
	}

	public void handleDocumentPreferences() {
		DocumentPreferencesDialog dlg = new DocumentPreferencesDialog(mainFrame);
		SwingUtil.showModalDialog(dlg, mainFrame);
	}

	public void handlePreferences() {
		PreferencesDialog dlg = new PreferencesDialog();
		SwingUtil.showModalDialog(dlg, mainFrame);
	}

	public void handleViewStatus(boolean selected) {
	}

	public void handleExportBookText() {
		BookExporter exp = new BookExporter(mainFrame);
		exp.setExportForOpenOffice(true);
		exp.exportToFile();
	}

	public void handleExportBookHtml() {
		BookExporter exp = new BookExporter(mainFrame);
		exp.setExportForOpenOffice(true);
		exp.exportToFile();
	}

	public void handleCopyBookText() {
		BookExporter exp = new BookExporter(mainFrame);
		exp.setExportForOpenOffice(false);
		exp.exportToClipboard();
	}

	public void handleCopyBlurb() {
		Internal internal = DocumentUtil.restoreInternal(mainFrame,
			InternalKey.BLURB, "");
		StringSelection selection = new StringSelection(
			internal.getStringValue() + "\n");
		Clipboard clbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		clbrd.setContents(selection, selection);
	}

	/* SB5 suppress Pro
	 public void handleGoPro() {
	 if (I18N.isGerman()) {
	 NetUtil.openBrowser(SbConstants.URL.GO_PRO_DE.toString());
	 return;
	 }
	 NetUtil.openBrowser(SbConstants.URL.GO_PRO_EN.toString());
	 }
	 */
	public void handleReportBug() {
		NetUtil.openBrowser(SbConstants.URL.REPORTBUG.toString());
	}

	public void handleDoc() {
		NetUtil.openBrowser(SbConstants.URL.DOC.toString());
	}

	public void handleFAQ() {
		NetUtil.openBrowser(SbConstants.URL.FAQ.toString());
	}

	public void handleHomepage() {
		NetUtil.openBrowser(SbConstants.URL.HOMEPAGE.toString());
	}

	public void handleFacebook() {
		/* SB5 never more FACEBOOK
		 NetUtil.openBrowser(SbConstants.URL.FACEBOOK.toString());
		 */
	}

	public void handleAbout() {
		AboutDialog dlg = new AboutDialog(mainFrame);
		SwingUtil.showModalDialog(dlg, mainFrame);
	}

	public void handleExit() {
		StorybookApp.getInstance().exit();
	}
}
