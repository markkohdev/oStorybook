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
package storybook.ui.dialog;

import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JList;
import org.hibernate.Session;
import storybook.model.BookModel;
import storybook.model.hbn.dao.ChapterDAOImpl;
import storybook.model.hbn.entity.Chapter;
import storybook.model.hbn.entity.Gender;
import storybook.model.hbn.entity.Location;
import storybook.model.hbn.entity.Person;
import storybook.model.hbn.entity.Scene;
import storybook.model.hbn.entity.Strand;
import storybook.ui.MainFrame;
import storybook.toolkit.I18N;

/**
 *
 * @author favdb
 */
public class CommonBox {

	public static void loadCbChapters(MainFrame mainFrame,JComboBox cb, Scene scene) {
		BookModel model = mainFrame.getDocumentModel();
		Session session = model.beginTransaction();
		ChapterDAOImpl dao = new ChapterDAOImpl(session);
		List<Chapter> chapters = dao.findAll();
		cb.removeAllItems();
		int ix=-1,i=0;
		for (Chapter chapter : chapters) {
			cb.addItem(chapter.getPart()+"."+chapter.getChapternoStr()+". "+chapter.getTitle());
			if (scene.getChapter().equals(chapter)) {
				ix=i;
			}
			i++;
		}
		cb.setSelectedIndex(ix);
		model.commit();
	}

	public static void loadCbStatus(JComboBox cb,Scene scene) {
		String[] lbStatus={
			I18N.getMsg("STATUS_OUTLINE"),
			I18N.getMsg("STATUS_DRAFT"),
			I18N.getMsg("STATUS_EDIT1"),
			I18N.getMsg("STATUS_EDIT2"),
			I18N.getMsg("STATUS_DONE")
		};
		for (String x : lbStatus) {
			cb.addItem(x);
		}
		cb.setSelectedItem(scene.getStatus());
	}

	public static void loadLbStrands(JList lb, Scene scene) {
		// TODO loadLbStrands
	}

	public static void loadCbGenders(JComboBox cb, Scene scene) {
		// TODO loadCbGenders
	}

	public static void loadLbPersons(JList lbPersons, Scene scene) {
		// TODO loadLbPersons
	}

	public static void loadLbLocations(JList lbLocations, Scene scene) {
		// TODO loadLbLocations
	}

}
