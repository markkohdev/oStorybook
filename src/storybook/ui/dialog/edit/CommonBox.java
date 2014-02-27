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

import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import org.hibernate.Session;
import storybook.model.BookModel;
import storybook.model.hbn.dao.AttributeDAOImpl;
import storybook.model.hbn.dao.CategoryDAOImpl;
import storybook.model.hbn.dao.ChapterDAOImpl;
import storybook.model.hbn.dao.GenderDAOImpl;
import storybook.model.hbn.dao.LocationDAOImpl;
import storybook.model.hbn.dao.PartDAOImpl;
import storybook.model.hbn.dao.PersonDAOImpl;
import storybook.model.hbn.dao.StrandDAOImpl;
import storybook.model.hbn.entity.Attribute;
import storybook.model.hbn.entity.Category;
import storybook.model.hbn.entity.Chapter;
import storybook.model.hbn.entity.Gender;
import storybook.model.hbn.entity.Location;
import storybook.model.hbn.entity.Part;
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

	public static void loadCbChapters(MainFrame mainFrame, JComboBox cb, Scene scene) {
		BookModel model = mainFrame.getDocumentModel();
		Session session = model.beginTransaction();
		ChapterDAOImpl dao = new ChapterDAOImpl(session);
		List<Chapter> chapters = dao.findAll();
		cb.removeAllItems();
		int ix=-1,i=0;
		for (Chapter chapter : chapters) {
			cb.addItem(chapter.getPart()+"."+chapter.getChapternoStr()+". "+chapter.getTitle());
			if ((scene.hasChapter()) && (scene.getChapter().equals(chapter))) {
				ix=i;
			}
			i++;
		}
		cb.setSelectedIndex(ix);
		model.commit();
	}

	public static void loadCbStatus(JComboBox cb,Scene scene) {
		String[] lbStatus={
			I18N.getMsg("msg.status.outline"),
			I18N.getMsg("msg.status.draft"),
			I18N.getMsg("msg.status.1st.edit"),
			I18N.getMsg("msg.status.2nd.edit"),
			I18N.getMsg("msg.status.done")
		};
		cb.removeAllItems();
		for (String x : lbStatus) {
			cb.addItem(x);
		}
		cb.setSelectedItem(scene.getStatus());
	}

	public static void loadLbStrands(MainFrame mainFrame, JList lb, Scene scene) {
		DefaultListModel listModel = new DefaultListModel();
		if (!"".equals(scene.getTitle())) {
			BookModel model = mainFrame.getDocumentModel();
			Session session = model.beginTransaction();
			StrandDAOImpl dao = new StrandDAOImpl(session);
			List<Strand> strands = dao.findAll();
			int ix=-1,i=0;
			for (Strand strand : strands) {
				listModel.addElement(strand.getName());
				if ((scene.getStrand()!=null) && (scene.getStrand().equals(strand))) {
					ix=i;
				}
				i++;
			}
			lb.setModel(listModel);
			lb.setSelectedIndex(ix);
			model.commit();
		} else lb.setModel(listModel);
	}

	public static void loadCbGenders(MainFrame mainFrame, JComboBox cb, Person person) {
		BookModel model = mainFrame.getDocumentModel();
		Session session = model.beginTransaction();
		GenderDAOImpl dao = new GenderDAOImpl(session);
		List<Gender> genders = dao.findAll();
		cb.removeAllItems();
		int ix=-1,i=0;
		for (Gender gender : genders) {
			cb.addItem(gender.getName());
			if ((!person.getFullName().equals(" ")) && (person.getGender().equals(gender))) {
				ix=i;
			}
			i++;
		}
		cb.setSelectedIndex(ix);
		model.commit();
	}

	public static void loadLbPersons(MainFrame mainFrame, JList lb, Scene scene) {
		DefaultListModel listModel = new DefaultListModel();
		if (!"".equals(scene.getTitle())) {
			BookModel model = mainFrame.getDocumentModel();
			Session session = model.beginTransaction();
			PersonDAOImpl dao = new PersonDAOImpl(session);
			List<Person> persons = dao.findAll();
			int ix=-1,i=0;
			int[] indices={};
			for (Person person : persons) {
				listModel.addElement(person.getFullName());
				if ((scene.getPersons()!=null) && (scene.getPersons().contains(person))) {
					indices[indices.length]=i;
				}
				i++;
			}
			lb.setModel(listModel);
			lb.setSelectedIndices(indices);
			model.commit();
		}  else lb.setModel(listModel);
	}

	public static void loadLbLocations(MainFrame mainFrame, JList lb, Scene scene) {
		DefaultListModel listModel = new DefaultListModel();
		if (!"".equals(scene.getTitle())) {
			BookModel model = mainFrame.getDocumentModel();
			Session session = model.beginTransaction();
			LocationDAOImpl dao = new LocationDAOImpl(session);
			List<Location> locations = dao.findAll();
			int ix=-1,i=0;
			int[] indices={};
			for (Location location : locations) {
				listModel.addElement(location.getFullName());
				if ((scene.getLocations()!=null) && (scene.getLocations().contains(location))) {
					indices[indices.length]=i;
				}
				i++;
			}
			lb.setModel(listModel);
			lb.setSelectedIndices(indices);
			model.commit();
		} else lb.setModel(listModel);
	}

	public static void loadCbParts(MainFrame mainFrame,JComboBox cb, Chapter chapter) {
		BookModel model = mainFrame.getDocumentModel();
		Session session = model.beginTransaction();
		PartDAOImpl dao = new PartDAOImpl(session);
		List<Part> parts = dao.findAll();
		cb.removeAllItems();
		int ix=-1,i=0;
		for (Part part : parts) {
			cb.addItem(part.getNumberName());
			if ((chapter.hasPart()) && (chapter.getPart().equals(part))) {
				ix=i;
			}
			i++;
		}
		cb.setSelectedIndex(ix);
		model.commit();
	}

	public static void loadCbCategory(MainFrame mainFrame,JComboBox cb, Person person) {
		BookModel model = mainFrame.getDocumentModel();
		Session session = model.beginTransaction();
		CategoryDAOImpl dao = new CategoryDAOImpl(session);
		List<Category> categories = dao.findAll();
		cb.removeAllItems();
		int ix=-1,i=0;
		for (Category category : categories) {
			cb.addItem(category.getName());
			if ((!person.getFullName().equals(" ")) && (person.getCategory().equals(category))) {
				ix=i;
			}
			i++;
		}
		cb.setSelectedIndex(ix);
		model.commit();
	}

	public static void loadLbAttributes(MainFrame mainFrame, JList lb, Person person) {
		DefaultListModel listModel = new DefaultListModel();
		if (person.getAttributes()!=null) {
			BookModel model = mainFrame.getDocumentModel();
			Session session = model.beginTransaction();
			AttributeDAOImpl dao = new AttributeDAOImpl(session);
			List<Attribute> attributes = dao.findAll();
			int ix=-1,i=0;
			for (Attribute attribute : attributes) {
				listModel.addElement(attribute.getKey()+":"+attribute.getValue());
				if ((!person.getFullName().equals("")) && (attribute.equals(attribute))) {
					ix=i;
				}
				i++;
			}
			lb.setModel(listModel);
			lb.setSelectedIndex(ix);
			model.commit();
		} else lb.setModel(listModel);
	}

	public static void loadCbCities(MainFrame mainFrame,JComboBox cb, Location location) {
		BookModel model = mainFrame.getDocumentModel();
		Session session = model.beginTransaction();
		LocationDAOImpl dao = new LocationDAOImpl(session);
		List<String> cities = dao.findCities();
		cb.removeAllItems();
		int ix=-1,i=0;
		for (String city : cities) {
			cb.addItem(city);
			if ((location.hasCity()) && (location.getCity().equals(city))) {
				ix=i;
			}
			i++;
		}
		cb.setSelectedIndex(ix);
		model.commit();
	}

	public static void loadCbCountries(MainFrame mainFrame,JComboBox cb, Location location) {
		BookModel model = mainFrame.getDocumentModel();
		Session session = model.beginTransaction();
		LocationDAOImpl dao = new LocationDAOImpl(session);
		List<String> countries = dao.findCountries();
		cb.removeAllItems();
		int ix=-1,i=0;
		for (String country : countries) {
			cb.addItem(country);
			if ((location.hasCountry()) && (location.getCountry().equals(country))) {
				ix=i;
			}
			i++;
		}
		cb.setSelectedIndex(ix);
		model.commit();
	}

}
