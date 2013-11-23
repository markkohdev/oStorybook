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

package storybook.toolkit;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.hibernate.Session;
import storybook.SbConstants;
import storybook.SbConstants.InternalKey;
import storybook.SbConstants.PreferenceKey;
import storybook.model.DbFile;
import storybook.model.DocumentModel;
import storybook.model.hbn.dao.InternalDAOImpl;
import storybook.model.hbn.entity.Internal;
import storybook.model.hbn.entity.Preference;
import storybook.toolkit.filefilter.H2FileFilter;
import storybook.view.MainFrame;

/**
 * @author martin
 *
 */
public class DocumentUtil {

	public static boolean isUseHtmlScenes(MainFrame mainFrame) {
		Internal internal = restoreInternal(mainFrame,
				InternalKey.USE_HTML_SCENES,
				SbConstants.DEFAULT_USE_HTML_SCENES);
		return internal.getBooleanValue();
	}

	public static boolean isUseHtmlDescr(MainFrame mainFrame) {
		Internal internal = restoreInternal(mainFrame,
				InternalKey.USE_HTML_DESCR, SbConstants.DEFAULT_USE_HTML_DESCR);
		return internal.getBooleanValue();
	}

	public static boolean isExportChapterNumbers(MainFrame mainFrame) {
		Internal internal = restoreInternal(mainFrame,
				InternalKey.EXPORT_CHAPTER_NUMBERS,
				SbConstants.DEFAULT_EXPORT_CHAPTER_NUMBERS);
		return internal.getBooleanValue();
	}

	public static boolean isExportRomanNumerals(MainFrame mainFrame) {
		Internal internal = restoreInternal(mainFrame,
				InternalKey.EXPORT_ROMAN_NUMERALS,
				SbConstants.DEFAULT_EXPORT_ROMAN_NUMERALS);
		return internal.getBooleanValue();
	}

	public static boolean isExportChapterTitles(MainFrame mainFrame) {
		Internal internal = restoreInternal(mainFrame,
				InternalKey.EXPORT_CHAPTER_TITLES,
				SbConstants.DEFAULT_EXPORT_CHAPTER_TITLES);
		return internal.getBooleanValue();
	}

	public static boolean isExportChapterDatesLocations(MainFrame mainFrame) {
		Internal internal = restoreInternal(mainFrame,
				InternalKey.EXPORT_CHAPTER_DATES_LOCATIONS,
				SbConstants.DEFAULT_EXPORT_CHAPTER_DATES_LOCATIONS);
		return internal.getBooleanValue();
	}

	public static boolean isExportSceneTitle(MainFrame mainFrame) {
		Internal internal = restoreInternal(mainFrame,
				InternalKey.EXPORT_SCENE_TITLES,
				SbConstants.DEFAULT_EXPORT_SCENE_TITLES);
		return internal.getBooleanValue();
	}

	public static boolean isExportPartTitles(MainFrame mainFrame) {
		Internal internal = restoreInternal(mainFrame,
				InternalKey.EXPORT_PART_TITLES,
				SbConstants.DEFAULT_EXPORT_PART_TITLES);
		return internal.getBooleanValue();
	}

	public static boolean isEditorFullToolbar(MainFrame mainFrame) {
		Internal internal = restoreInternal(mainFrame,
				InternalKey.EDITOR_FULL_TOOLBAR,
				SbConstants.DEFAULT_EDITOR_FULL_TOOLBAR);
		return internal.getBooleanValue();
	}

	public static void storeInternal(MainFrame mainFrame, InternalKey key,
			Object val) {
		DocumentModel model = mainFrame.getDocumentModel();
		Session session = model.beginTransaction();
		InternalDAOImpl dao = new InternalDAOImpl(session);
		dao.saveOrUpdate(key.toString(), val);
		model.commit();
	}

	public static void storeInternal(MainFrame mainFrame, String strKey,
			Object val) {
		DocumentModel model = mainFrame.getDocumentModel();
		Session session = model.beginTransaction();
		InternalDAOImpl dao = new InternalDAOImpl(session);
		dao.saveOrUpdate(strKey, val);
		model.commit();
	}

	public static Internal restoreInternal(MainFrame mainFrame,
			InternalKey key, Object defaultValue) {
		DocumentModel model = mainFrame.getDocumentModel();
		Session session = model.beginTransaction();
		InternalDAOImpl dao = new InternalDAOImpl(session);
		Internal internal = dao.findByKey(key.toString());
		if (internal == null) {
			internal = new Internal(key.toString(), defaultValue);
			session.save(internal);
		}
		model.commit();
		return internal;
	}

	public static Internal restoreInternal(MainFrame mainFrame, String strKey,
			Object defaultValue) {
		DocumentModel model = mainFrame.getDocumentModel();
		Session session = model.beginTransaction();
		InternalDAOImpl dao = new InternalDAOImpl(session);
		Internal internal = dao.findByKey(strKey);
		if (internal == null) {
			internal = new Internal(strKey, defaultValue);
			session.save(internal);
		}
		model.commit();
		return internal;
	}

	public static DbFile openDocumentDialog() {
		final JFileChooser fc = new JFileChooser();

		Preference pref = PrefUtil.get(PreferenceKey.LAST_OPEN_DIR,
				getHomeDir());
		fc.setCurrentDirectory(new File(pref.getStringValue()));

		H2FileFilter filter = new H2FileFilter();
		fc.addChoosableFileFilter(filter);
		fc.setFileFilter(filter);
		int ret = fc.showOpenDialog(null);
		if (ret == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if (!file.exists()) {
				JOptionPane.showMessageDialog(null,
						I18N.getMsg("msg.dlg.project.not.exits.text", file),
						I18N.getMsg("msg.dlg.project.not.exits.title"),
						JOptionPane.ERROR_MESSAGE);
				return null;
			}
			DbFile dbFile = new DbFile(file);
			return dbFile;
		}
		return null;
	}

	public static String getHomeDir() {
		return System.getProperty("user.home");
	}
}
