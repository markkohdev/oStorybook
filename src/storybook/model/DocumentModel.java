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

package storybook.model;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import storybook.SbConstants.ViewName;
import storybook.controller.DocumentController;
import storybook.model.hbn.dao.CategoryDAOImpl;
import storybook.model.hbn.dao.ChapterDAOImpl;
import storybook.model.hbn.dao.GenderDAOImpl;
import storybook.model.hbn.dao.IdeaDAOImpl;
import storybook.model.hbn.dao.InternalDAOImpl;
import storybook.model.hbn.dao.ItemDAOImpl;
import storybook.model.hbn.dao.ItemLinkDAOImpl;
import storybook.model.hbn.dao.LocationDAOImpl;
import storybook.model.hbn.dao.PartDAOImpl;
import storybook.model.hbn.dao.PersonDAOImpl;
import storybook.model.hbn.dao.SceneDAOImpl;
import storybook.model.hbn.dao.StrandDAOImpl;
import storybook.model.hbn.dao.TagDAOImpl;
import storybook.model.hbn.dao.TagLinkDAOImpl;
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
import storybook.model.state.SceneState;
import storybook.toolkit.I18N;
import storybook.toolkit.swing.ColorUtil;
import storybook.view.SbView;
import storybook.view.book.BookPanel;
import storybook.view.chrono.ChronoPanel;
import storybook.view.manage.ManagePanel;
import storybook.view.reading.ReadingPanel;

/**
 * @author martin
 *
 */
public class DocumentModel extends AbstractModel {

	public synchronized void initEntites() {
		Session session = beginTransaction();

		// default strand
		Strand strand = new Strand();
		strand.setName(I18N.getMsg("db.init.strand.name"));
		strand.setAbbreviation(I18N.getMsg("db.init.strand.abbr"));
		strand.setSort(1);
		strand.setJColor(ColorUtil.getNiceBlue());
		strand.setNotes("");
		session.save(strand);

		// default part
		Part part = new Part(1, I18N.getMsg("db.init.part"));
		session.save(part);

		// first chapter
		Chapter chapter = new Chapter();
		chapter.setPart(part);
		chapter.setChapterno(1);
		chapter.setTitle(I18N.getMsg("msg.common.chapter") + " 1");
		chapter.setDescription("");
		chapter.setNotes("");
		session.save(chapter);

		// first scene
		Scene scene = EntityUtil.createScene(strand, chapter);
		session.save(scene);

		// default genders
		Gender male = new Gender(I18N.getMsg("msg.dlg.person.gender.male"), 12,
				6, 47, 14);
		session.save(male);
		Gender female = new Gender(I18N.getMsg("msg.dlg.person.gender.female"),
				12, 6, 47, 14);
		session.save(female);

		// default categories
		Category major = new Category(1,
				I18N.getMsg("msg.category.central.character"));
		session.save(major);
		Category minor = new Category(2,
				I18N.getMsg("msg.category.minor.character"));
		session.save(minor);

		commit();
	}

	public synchronized void initSession(String dbName) {
		try {
			super.initSession(dbName, "hibernate.cfg.xml");
			Session session = beginTransaction();
			// test queries
			sessionFactory.query(new PartDAOImpl(session));
			sessionFactory.query(new ChapterDAOImpl(session));
			commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void fireAgain() {
		fireAgainScenes();
		fireAgainChapters();
		fireAgainParts();

		fireAgainLocations();

		fireAgainPersons();
		fireAgainGenders();
		fireAgainCategories();

		fireAgainStrands();
		fireAgainIdeas();

		fireAgainTags();
		fireAgainItems();
		fireAgainTagLinks();
		fireAgainItemLinks();

		fireAgainInternals();
	}

	public void fireAgain(SbView view) {
		if (ViewName.CHRONO.compare(view)) {
			fireAgainScenes();
		} else if (ViewName.BOOK.compare(view)) {
			fireAgainScenes();
		} else if (ViewName.READING.compare(view)) {
			fireAgainChapters();
		} else if (ViewName.MANAGE.compare(view)) {
			fireAgainChapters();
		} else if (ViewName.SCENES.compare(view)) {
			fireAgainScenes();
		} else if (ViewName.CHAPTERS.compare(view)) {
			fireAgainChapters();
		} else if (ViewName.PARTS.compare(view)) {
			fireAgainParts();
		} else if (ViewName.LOCATIONS.compare(view)) {
			fireAgainLocations();
		} else if (ViewName.PERSONS.compare(view)) {
			fireAgainPersons();
		} else if (ViewName.GENDERS.compare(view)) {
			fireAgainGenders();
		} else if (ViewName.CATEGORIES.compare(view)) {
			fireAgainCategories();
		} else if (ViewName.STRANDS.compare(view)) {
			fireAgainStrands();
		} else if (ViewName.IDEAS.compare(view)) {
			fireAgainIdeas();
		} else if (ViewName.TAGS.compare(view)) {
			fireAgainTags();
		} else if (ViewName.ITEMS.compare(view)) {
			fireAgainItems();
		} else if (ViewName.TAGLINKS.compare(view)) {
			fireAgainTagLinks();
		} else if (ViewName.ITEMLINKS.compare(view)) {
			fireAgainItemLinks();
		} else if (ViewName.INTERNALS.compare(view)) {
			fireAgainInternals();
		}
	}

	private void fireAgainScenes() {
		Session session = beginTransaction();
		SceneDAOImpl sceneDao = new SceneDAOImpl(session);
		List<Scene> scenes = sceneDao.findAll();
		commit();
		firePropertyChange(DocumentController.SceneProps.INIT.toString(), null,
				scenes);
	}

	private void fireAgainChapters() {
		Session session = beginTransaction();
		ChapterDAOImpl chapterDao = new ChapterDAOImpl(session);
		List<Chapter> chapters = chapterDao.findAll();
		commit();
		firePropertyChange(DocumentController.ChapterProps.INIT.toString(),
				null, chapters);
	}

	private void fireAgainParts() {
		Session session = beginTransaction();
		PartDAOImpl partDao = new PartDAOImpl(session);
		List<Part> parts = partDao.findAll();
		commit();
		firePropertyChange(DocumentController.PartProps.INIT.toString(), null,
				parts);
	}

	private void fireAgainLocations() {
		Session session = beginTransaction();
		LocationDAOImpl locationDao = new LocationDAOImpl(session);
		List<Location> locations = locationDao.findAll();
		commit();
		firePropertyChange(DocumentController.LocationProps.INIT.toString(),
				null, locations);
	}

	private void fireAgainPersons() {
		Session session = beginTransaction();
		PersonDAOImpl personDao = new PersonDAOImpl(session);
		List<Person> persons = personDao.findAll();
		commit();
		firePropertyChange(DocumentController.PersonProps.INIT.toString(),
				null, persons);
	}

	private void fireAgainGenders() {
		Session session = beginTransaction();
		GenderDAOImpl genderDao = new GenderDAOImpl(session);
		List<Gender> genders = genderDao.findAll();
		commit();
		firePropertyChange(DocumentController.GenderProps.INIT.toString(),
				null, genders);
	}

	private void fireAgainCategories() {
		Session session = beginTransaction();
		CategoryDAOImpl categoryDao = new CategoryDAOImpl(session);
		List<Category> categories = categoryDao.findAll();
		commit();
		firePropertyChange(DocumentController.CategoryProps.INIT.toString(),
				null, categories);
	}

	private void fireAgainStrands() {
		Session session = beginTransaction();
		StrandDAOImpl strandDao = new StrandDAOImpl(session);
		List<Strand> strands = strandDao.findAll();
		commit();
		firePropertyChange(DocumentController.StrandProps.INIT.toString(),
				null, strands);
	}

	private void fireAgainIdeas() {
		Session session = beginTransaction();
		IdeaDAOImpl ideaDao = new IdeaDAOImpl(session);
		List<Idea> ideas = ideaDao.findAll();
		commit();
		firePropertyChange(DocumentController.IdeaProps.INIT.toString(), null,
				ideas);
	}

	private void fireAgainTags() {
		Session session = beginTransaction();
		TagDAOImpl tagDao = new TagDAOImpl(session);
		List<Tag> tags = tagDao.findAll();
		commit();
		firePropertyChange(DocumentController.TagProps.INIT.toString(), null,
				tags);
	}

	private void fireAgainItems() {
		Session session = beginTransaction();
		ItemDAOImpl itemDao = new ItemDAOImpl(session);
		List<Item> items = itemDao.findAll();
		commit();
		firePropertyChange(DocumentController.ItemProps.INIT.toString(), null,
				items);
	}

	private void fireAgainTagLinks() {
		Session session = beginTransaction();
		TagLinkDAOImpl tagLinkDao = new TagLinkDAOImpl(session);
		List<TagLink> tagLinks = tagLinkDao.findAll();
		commit();
		firePropertyChange(DocumentController.TagLinkProps.INIT.toString(),
				null, tagLinks);
	}

	private void fireAgainItemLinks() {
		Session session = beginTransaction();
		ItemLinkDAOImpl itemLinkDao = new ItemLinkDAOImpl(session);
		List<ItemLink> itemLinks = itemLinkDao.findAll();
		commit();
		firePropertyChange(DocumentController.ItemLinkProps.INIT.toString(),
				null, itemLinks);
	}

	private void fireAgainInternals() {
		Session session = beginTransaction();
		InternalDAOImpl internalDao = new InternalDAOImpl(session);
		List<Internal> internals = internalDao.findAll();
		commit();
		firePropertyChange(DocumentController.InternalProps.INIT.toString(),
				null, internals);
	}

	// common
	public void setRefresh(SbView view) {
		firePropertyChange(DocumentController.CommonProps.REFRESH.toString(),
				null, view);
		try {
			if (view.getComponentCount() == 0) {
				return;
			}
			Component comp = view.getComponent();
			if (comp instanceof ChronoPanel || comp instanceof BookPanel
					|| comp instanceof ManagePanel
					|| comp instanceof ReadingPanel) {
				// these views don't need a "fire again"
				return;
			}
			fireAgain(view);
		} catch (ArrayIndexOutOfBoundsException e) {
			// ignore
			return;
		}
	}

	public void setShowOptions(SbView view) {
		firePropertyChange(
				DocumentController.CommonProps.SHOW_OPTIONS.toString(), null,
				view);
	}

	public void setShowInfo(Scene scene) {
		setShowInfo((AbstractEntity)scene);
	}

	public void setShowInfo(Chapter chapter) {
		setShowInfo((AbstractEntity)chapter);
	}

	public void setShowInfo(Part part) {
		setShowInfo((AbstractEntity)part);
	}

	public void setShowInfo(Person person) {
		setShowInfo((AbstractEntity)person);
	}

	public void setShowInfo(Category category) {
		setShowInfo((AbstractEntity)category);
	}

	public void setShowInfo(Gender gender) {
		setShowInfo((AbstractEntity)gender);
	}

	public void setShowInfo(Location location) {
		setShowInfo((AbstractEntity)location);
	}

	public void setShowInfo(Tag tag) {
		setShowInfo((AbstractEntity)tag);
	}

	public void setShowInfo(TagLink tagLink) {
		setShowInfo((AbstractEntity)tagLink);
	}

	public void setShowInfo(Item item) {
		setShowInfo((AbstractEntity)item);
	}

	public void setShowInfo(ItemLink itemLink) {
		setShowInfo((AbstractEntity)itemLink);
	}

	public void setShowInfo(Strand strand) {
		setShowInfo((AbstractEntity) strand);
	}

	public void setShowInfo(Idea idea) {
		setShowInfo((AbstractEntity) idea);
	}

	public void setShowInfo(AbstractEntity entity) {
		firePropertyChange(DocumentController.CommonProps.SHOW_INFO.toString(),
				null, entity);
	}

	public void setShowInfo(DbFile dbFile) {
		firePropertyChange(DocumentController.CommonProps.SHOW_INFO.toString(),
				null, dbFile);
	}

	public void setShowInMemoria(Person person) {
		setShowInMemoria((AbstractEntity) person);
	}

	public void setShowInMemoria(Location location) {
		setShowInMemoria((AbstractEntity) location);
	}

	public void setShowInMemoria(Scene scene) {
		setShowInMemoria((AbstractEntity) scene);
	}

	public void setShowInMemoria(Tag tag) {
		setShowInMemoria((AbstractEntity) tag);
	}

	public void setShowInMemoria(Item item) {
		setShowInMemoria((AbstractEntity) item);
	}

	public void setShowInMemoria(AbstractEntity entity) {
		firePropertyChange(
				DocumentController.CommonProps.SHOW_IN_MEMORIA.toString(),
				null, entity);
	}

	public void setUnloadEditor() {
		firePropertyChange(
				DocumentController.CommonProps.UNLOAD_EDITOR.toString(), null,
				null);
	}

	public void setFilterScenes(SceneState state) {
		firePropertyChange(DocumentController.SceneProps.FILTER.toString(),
				null, state);
	}

	public void setPrint(SbView view) {
		firePropertyChange(DocumentController.CommonProps.PRINT.toString(),
				null, view);
	}

	public void setExport(SbView view) {
		firePropertyChange(DocumentController.CommonProps.EXPORT.toString(),
				null, view);
	}

	// chrono view
	public void setChronoZoom(Integer val) {
		firePropertyChange(DocumentController.ChronoViewProps.ZOOM.toString(),
				null, val);
	}

	public void setChronoLayoutDirection(Boolean val) {
		firePropertyChange(
				DocumentController.ChronoViewProps.LAYOUT_DIRECTION.toString(),
				null, val);
	}

	public void setChronoShowDateDifference(Boolean val) {
		firePropertyChange(
				DocumentController.ChronoViewProps.SHOW_DATE_DIFFERENCE
						.toString(),
				null, val);
	}

	public void setChronoShowEntity(Scene scene) {
		firePropertyChange(
				DocumentController.ChronoViewProps.SHOW_ENTITY.toString(),
				null, scene);
	}

	public void setChronoShowEntity(Chapter chapter) {
		firePropertyChange(
				DocumentController.ChronoViewProps.SHOW_ENTITY.toString(),
				null, chapter);
	}

	// book view
	public void setBookZoom(Integer val) {
		firePropertyChange(DocumentController.BookViewProps.ZOOM.toString(),
				null, val);
	}

	public void setBookHeightFactor(Integer val) {
		firePropertyChange(
				DocumentController.BookViewProps.HEIGHT_FACTOR.toString(),
				null, val);
	}

	public void setBookShowEntity(Scene scene) {
		firePropertyChange(
				DocumentController.BookViewProps.SHOW_ENTITY.toString(), null,
				scene);
	}

	public void setBookShowEntity(Chapter chapter) {
		firePropertyChange(
				DocumentController.BookViewProps.SHOW_ENTITY.toString(), null,
				chapter);
	}

	// manage view
	public void setManageZoom(Integer val) {
		firePropertyChange(DocumentController.ManageViewProps.ZOOM.toString(),
				null, val);
	}

	public void setManageColumns(Integer val) {
		firePropertyChange(DocumentController.ManageViewProps.COLUMNS.toString(),
				null, val);
	}

	public void setManageShowEntity(Scene scene) {
		firePropertyChange(
				DocumentController.ManageViewProps.SHOW_ENTITY.toString(),
				null, scene);
	}

	public void setManageShowEntity(Chapter chapter) {
		firePropertyChange(
				DocumentController.ManageViewProps.SHOW_ENTITY.toString(),
				null, chapter);
	}

	// reading view
	public void setReadingZoom(Integer val) {
		firePropertyChange(DocumentController.ReadingViewProps.ZOOM.toString(),
				null, val);
	}

	public void setReadingFontSize(Integer val) {
		firePropertyChange(
				DocumentController.ReadingViewProps.FONT_SIZE.toString(), null,
				val);
	}

	// memoria view
	public void setMemoriaBalloon(Boolean val) {
		firePropertyChange(
				DocumentController.MemoriaViewProps.BALLOON.toString(), null,
				val);
	}

	// chapter
	public void setEditChapter(Chapter editChapter) {
		firePropertyChange(DocumentController.ChapterProps.EDIT.toString(),
				null, editChapter);
	}

	public synchronized void setUpdateChapter(Chapter chapter) {
		Session session = beginTransaction();
		ChapterDAOImpl dao = new ChapterDAOImpl(session);
		Chapter old = dao.find(chapter.getId());
		commit();

		session = beginTransaction();
		session.update(chapter);
		commit();

		firePropertyChange(DocumentController.ChapterProps.UPDATE.toString(),
				old, chapter);
	}

	public synchronized void setNewChapter(Chapter chapter) {
		Session session = beginTransaction();
		session.save(chapter);
		commit();

		firePropertyChange(DocumentController.ChapterProps.NEW.toString(),
				null, chapter);
	}

	public synchronized void setDeleteChapter(Chapter chapter) {
		if (chapter.getId() == null) {
			return;
		}

		Session session = beginTransaction();
		// find scenes, set chapter to null
		ChapterDAOImpl dao = new ChapterDAOImpl(session);
		List<Scene> scenes = dao.findScenes(chapter);
		commit();
		for (Scene scene : scenes) {
			scene.setChapter();
			setUpdateScene(scene);
		}
		// delete chapter
		session = beginTransaction();
		session.delete(chapter);
		commit();

		firePropertyChange(DocumentController.ChapterProps.DELETE.toString(),
				chapter, null);
	}

	public synchronized void setDeleteMultiChapters(ArrayList<Long> ids) {
		for (Long id : ids) {
			Session session = beginTransaction();
			ChapterDAOImpl dao = new ChapterDAOImpl(session);
			Chapter old = dao.find(id);
			commit();

			session = beginTransaction();
			dao = new ChapterDAOImpl(session);
			dao.removeById(id);
			commit();

			firePropertyChange(
					DocumentController.ChapterProps.DELETE.toString(), old,
					null);
		}
	}

	// part
	public void setEditPart(Part editPart) {
		firePropertyChange(DocumentController.PartProps.EDIT.toString(), null,
				editPart);
	}

	public synchronized void setUpdatePart(Part part) {
		Session session = beginTransaction();
		PartDAOImpl dao = new PartDAOImpl(session);
		Part old = dao.find(part.getId());
		commit();

		session = beginTransaction();
		session.update(part);
		commit();

		firePropertyChange(DocumentController.PartProps.UPDATE.toString(), old,
				part);
	}

	public synchronized void setNewPart(Part part) {
		Session session = beginTransaction();
		session.save(part);
		commit();

		firePropertyChange(DocumentController.PartProps.NEW.toString(), null,
				part);
	}

	public synchronized void setDeletePart(Part part) {
		if (part.getId() == null) {
			return;
		}

		Session session = beginTransaction();
		// delete chapters
		PartDAOImpl dao = new PartDAOImpl(session);
		List<Chapter> chapters = dao.findChapters(part);
		commit();
		for (Chapter chapter : chapters) {
			setDeleteChapter(chapter);
		}
		// delete part
		session = beginTransaction();
		session.delete(part);
		commit();

		firePropertyChange(DocumentController.PartProps.DELETE.toString(),
				part, null);
	}

	public synchronized void setDeleteMultiParts(ArrayList<Long> ids) {
		for (Long id : ids) {
			Session session = beginTransaction();
			PartDAOImpl dao = new PartDAOImpl(session);
			Part old = dao.find(id);
			commit();
			setDeletePart(old);
		}
	}

	public synchronized void setChangePart(Part part) {
		firePropertyChange(DocumentController.PartProps.CHANGE.toString(),
				null, part);
	}

	// location
	public void setEditLocation(Location editLocation) {
		firePropertyChange(DocumentController.LocationProps.EDIT.toString(),
				null, editLocation);
	}

	public synchronized void setUpdateLocation(Location location) {
		Session session = beginTransaction();
		LocationDAOImpl dao = new LocationDAOImpl(session);
		Location old = dao.find(location.getId());
		commit();

		session = beginTransaction();
		session.update(location);
		commit();

		firePropertyChange(DocumentController.LocationProps.UPDATE.toString(),
				old, location);
	}

	public synchronized void setNewLocation(Location location) {
		Session session = beginTransaction();
		session.save(location);
		commit();

		firePropertyChange(DocumentController.LocationProps.NEW.toString(),
				null, location);
	}

	public synchronized void setDeleteLocation(Location location) {
		if (location.getId() == null) {
			return;
		}
		try {
			// delete scene links
			Session session = beginTransaction();
			SceneDAOImpl dao = new SceneDAOImpl(session);
			List<Scene> scenes = dao.findByLocationLink(location);
			for (Scene scene : scenes) {
				scene.getLocations().remove(location);
				session.update(scene);
			}
			commit();
			for (Scene scene : scenes) {
				setUpdateScene(scene);
			}

			// delete tag / item links
			EntityUtil.deleteTagAndItemLinks(this, location);

			// delete location
			session = beginTransaction();
			session.delete(location);
			commit();
		} catch (ConstraintViolationException e) {
			e.printStackTrace();
		}
		firePropertyChange(DocumentController.LocationProps.DELETE.toString(),
				location, null);
	}

	public synchronized void setDeleteMultiLocations(ArrayList<Long> ids) {
		for (Long id : ids) {
			Session session = beginTransaction();
			LocationDAOImpl dao = new LocationDAOImpl(session);
			Location old = dao.find(id);
			commit();
			setDeleteLocation(old);
		}
	}

	// person
	public void setEditPerson(Person editPerson) {
		firePropertyChange(DocumentController.PersonProps.EDIT.toString(),
				null, editPerson);
	}

	public synchronized void setUpdatePerson(Person person) {
		Session session = beginTransaction();
		PersonDAOImpl dao = new PersonDAOImpl(session);
		Person old = dao.find(person.getId());
		commit();

		session = beginTransaction();
		session.update(person);
		commit();

		firePropertyChange(DocumentController.PersonProps.UPDATE.toString(),
				old, person);
	}

	public synchronized void setNewPerson(Person person) {
		Session session = beginTransaction();
		session.save(person);
		commit();

		firePropertyChange(DocumentController.PersonProps.NEW.toString(),
				null, person);
	}

	public synchronized void setDeletePerson(Person person) {
		if (person.getId() == null) {
			return;
		}
		try {
			// delete scene links
			Session session = beginTransaction();
			SceneDAOImpl dao = new SceneDAOImpl(session);
			List<Scene> scenes = dao.findByPersonLink(person);
			for (Scene scene : scenes) {
				scene.getPersons().remove(person);
				session.update(scene);
			}
			commit();
			for (Scene scene : scenes) {
				setUpdateScene(scene);
			}

			// delete tag / item links
			EntityUtil.deleteTagAndItemLinks(this, person);

			// delete person
			session = beginTransaction();
			session.delete(person);
			commit();
		} catch (ConstraintViolationException e) {
			e.printStackTrace();
		}
		firePropertyChange(DocumentController.PersonProps.DELETE.toString(),
				person, null);
	}

	public synchronized void setDeleteMultiPersons(ArrayList<Long> ids) {
		for (Long id : ids) {
			Session session = beginTransaction();
			PersonDAOImpl dao = new PersonDAOImpl(session);
			Person old = dao.find(id);
			commit();
			setDeletePerson(old);
		}
	}

	// gender
	public void setEditGender(Gender editGender) {
		firePropertyChange(DocumentController.GenderProps.EDIT.toString(),
				null, editGender);
	}

	public synchronized void setUpdateGender(Gender gender) {
		Session session = beginTransaction();
		GenderDAOImpl dao = new GenderDAOImpl(session);
		Gender old = dao.find(gender.getId());
		commit();

		session = beginTransaction();
		session.update(gender);
		commit();

		firePropertyChange(DocumentController.GenderProps.UPDATE.toString(),
				old, gender);
	}

	public synchronized void setNewGender(Gender gender) {
		Session session = beginTransaction();
		session.save(gender);
		commit();

		firePropertyChange(DocumentController.GenderProps.NEW.toString(), null,
				gender);
	}

	public synchronized void setDeleteGender(Gender gender) {
		if (gender.getId() == null) {
			return;
		}

		// set gender of affected persons to "male"
		Session session = beginTransaction();
		GenderDAOImpl dao = new GenderDAOImpl(session);
		Gender male = dao.findMale();
		List<Person> persons = dao.findPersons(gender);
		commit();
		for (Person person : persons) {
			person.setGender(male);
			setUpdatePerson(person);
		}
		// delete gender
		session = beginTransaction();
		session.delete(gender);
		commit();

		firePropertyChange(DocumentController.GenderProps.DELETE.toString(),
				gender, null);
	}

	public synchronized void setDeleteMultiGenders(ArrayList<Long> ids) {
		for (Long id : ids) {
			Session session = beginTransaction();
			GenderDAOImpl dao = new GenderDAOImpl(session);
			Gender old = dao.find(id);
			commit();

			session = beginTransaction();
			dao = new GenderDAOImpl(session);
			dao.removeById(id);
			commit();

			firePropertyChange(
					DocumentController.GenderProps.DELETE.toString(), old, null);
		}
	}

	// category
	public void setEditCategory(Category editCategory) {
		firePropertyChange(DocumentController.CategoryProps.EDIT.toString(),
				null, editCategory);
	}

	public synchronized void setUpdateCategory(Category category) {
		Session session = beginTransaction();
		CategoryDAOImpl dao = new CategoryDAOImpl(session);
		Category old = dao.find(category.getId());
		commit();

		session = beginTransaction();
		session.update(category);
		commit();

		firePropertyChange(DocumentController.CategoryProps.UPDATE.toString(),
				old, category);
	}

	public synchronized void setNewCategory(Category category) {
		Session session = beginTransaction();
		session.save(category);
		commit();

		firePropertyChange(DocumentController.CategoryProps.NEW.toString(),
				null, category);
	}

	public synchronized void setDeleteCategory(Category category) {
		if (category.getId() == null) {
			return;
		}

		// set category of affected persons to "minor"
		Session session = beginTransaction();
		CategoryDAOImpl dao = new CategoryDAOImpl(session);
		Category minor = dao.findMinor();
		List<Person> persons = dao.findPersons(category);
		commit();
		for (Person person : persons) {
			person.setCategory(minor);
			setUpdatePerson(person);
		}
		// delete category
		session = beginTransaction();
		session.delete(category);
		commit();

		firePropertyChange(DocumentController.CategoryProps.DELETE.toString(),
				category, null);
	}

	public synchronized void setDeleteMultiCategories(ArrayList<Long> ids) {
		for (Long id : ids) {
			Session session = beginTransaction();
			CategoryDAOImpl dao = new CategoryDAOImpl(session);
			Category old = dao.find(id);
			commit();

			session = beginTransaction();
			dao = new CategoryDAOImpl(session);
			dao.removeById(id);
			commit();

			firePropertyChange(
					DocumentController.CategoryProps.DELETE.toString(), old,
					null);
		}
	}

	public synchronized void setOrderUpCategory(Category category) {
		firePropertyChange(
				DocumentController.CategoryProps.ORDER_UP.toString(), null,
				category);
	}

	public synchronized void setOrderDownCategory(Category category) {
		firePropertyChange(
				DocumentController.CategoryProps.ORDER_DOWN.toString(), null,
				category);
	}

	// strand
	public void setEditStrand(Strand editStrand) {
		firePropertyChange(DocumentController.StrandProps.EDIT.toString(),
				null, editStrand);
	}

	public synchronized void setUpdateStrand(Strand strand) {
		Session session = beginTransaction();
		StrandDAOImpl dao = new StrandDAOImpl(session);
		Strand old = dao.find(strand.getId());
		commit();

		session = beginTransaction();
		session.update(strand);
		commit();

		firePropertyChange(DocumentController.StrandProps.UPDATE.toString(),
				old, strand);
	}

	public synchronized void setNewStrand(Strand strand) {
		Session session = beginTransaction();
		session.save(strand);
		commit();

		firePropertyChange(DocumentController.StrandProps.NEW.toString(), null,
				strand);
	}

	public synchronized void setDeleteStrand(Strand strand) {
		if (strand.getId() == null) {
			return;
		}
		try {
			// delete scene links
			Session session = beginTransaction();
			SceneDAOImpl sceneDao = new SceneDAOImpl(session);
			List<Scene> scenes = sceneDao.findByStrandLink(strand);
			for (Scene scene : scenes) {
				scene.getStrands().remove(strand);
				session.update(scene);
			}
			commit();
			for (Scene scene : scenes) {
				setUpdateScene(scene);
			}

			// delete scenes
			session = beginTransaction();
			StrandDAOImpl strandDao = new StrandDAOImpl(session);
			scenes = strandDao.findScenes(strand);
			commit();
			for (Scene scene : scenes) {
				setDeleteScene(scene);
			}

			// delete strand
			session = beginTransaction();
			session.delete(strand);
			commit();
		} catch (ConstraintViolationException e) {
			e.printStackTrace();
		}
		firePropertyChange(DocumentController.StrandProps.DELETE.toString(),
				strand, null);
	}

	public synchronized void setDeleteMultiStrands(ArrayList<Long> ids) {
		for (Long id : ids) {
			Session session = beginTransaction();
			StrandDAOImpl dao = new StrandDAOImpl(session);
			Strand old = dao.find(id);
			commit();
			setDeleteStrand(old);
		}
	}

	public synchronized void setOrderUpStrand(Strand strand) {
		firePropertyChange(DocumentController.StrandProps.ORDER_UP.toString(),
				null, strand);
	}

	public synchronized void setOrderDownStrand(Strand strand) {
		firePropertyChange(
				DocumentController.StrandProps.ORDER_DOWN.toString(), null,
				strand);
	}


	// idea
	public void setEditIdea(Idea editIdea) {
		firePropertyChange(DocumentController.IdeaProps.EDIT.toString(), null,
				editIdea);
	}

	public synchronized void setUpdateIdea(Idea idea) {
		Session session = beginTransaction();
		IdeaDAOImpl dao = new IdeaDAOImpl(session);
		Idea old = dao.find(idea.getId());
		commit();

		session = beginTransaction();
		session.update(idea);
		commit();

		firePropertyChange(DocumentController.IdeaProps.UPDATE.toString(), old,
				idea);
	}

	public synchronized void setNewIdea(Idea idea) {
		Session session = beginTransaction();
		session.save(idea);
		commit();

		firePropertyChange(DocumentController.IdeaProps.NEW.toString(), null,
				idea);
	}

	public synchronized void setDeleteIdea(Idea idea) {
		if (idea.getId() == null) {
			return;
		}
		Session session = beginTransaction();
		session.delete(idea);
		commit();
		firePropertyChange(DocumentController.IdeaProps.DELETE.toString(),
				idea, null);
	}

	public synchronized void setDeleteMultiIdeas(ArrayList<Long> ids) {
		for (Long id : ids) {
			Session session = beginTransaction();
			IdeaDAOImpl dao = new IdeaDAOImpl(session);
			Idea old = dao.find(id);
			commit();

			session = beginTransaction();
			dao = new IdeaDAOImpl(session);
			dao.removeById(id);
			commit();

			firePropertyChange(DocumentController.IdeaProps.DELETE.toString(),
					old, null);
		}
	}

	// tags
	public void setEditTag(Tag editTag) {
		firePropertyChange(DocumentController.TagProps.EDIT.toString(), null,
				editTag);
	}

	public synchronized void setUpdateTag(Tag tag) {
		Session session = beginTransaction();
		TagDAOImpl dao = new TagDAOImpl(session);
		Tag old = dao.find(tag.getId());
		commit();

		session = beginTransaction();
		session.update(tag);
		commit();

		firePropertyChange(DocumentController.TagProps.UPDATE.toString(), old,
				tag);
	}

	public synchronized void setNewTag(Tag tag) {
		Session session = beginTransaction();
		session.save(tag);
		commit();

		firePropertyChange(DocumentController.TagProps.NEW.toString(), null,
				tag);
	}

	public synchronized void setDeleteTag(Tag tag) {
		if (tag.getId() == null) {
			return;
		}
		// delete tag assignments
		Session session = beginTransaction();
		TagLinkDAOImpl dao = new TagLinkDAOImpl(session);
		List<TagLink> links = dao.findByTag(tag);
		commit();
		for (TagLink link : links) {
			setDeleteTagLink(link);
		}
		// delete tag
		session = beginTransaction();
		session.delete(tag);
		commit();
		firePropertyChange(DocumentController.TagProps.DELETE.toString(), tag,
				null);
	}

	public synchronized void setDeleteMultiTags(ArrayList<Long> ids) {
		for (Long id : ids) {
			Session session = beginTransaction();
			TagDAOImpl dao = new TagDAOImpl(session);
			Tag old = dao.find(id);
			commit();
			setDeleteTag(old);
		}
	}

	// items
	public void setEditItem(Item editItem) {
		firePropertyChange(DocumentController.ItemProps.EDIT.toString(), null,
				editItem);
	}

	public synchronized void setUpdateItem(Item item) {
		Session session = beginTransaction();
		ItemDAOImpl dao = new ItemDAOImpl(session);
		Item old = dao.find(item.getId());
		commit();

		session = beginTransaction();
		session.update(item);
		commit();

		firePropertyChange(DocumentController.ItemProps.UPDATE.toString(), old,
				item);
	}

	public synchronized void setNewItem(Item item) {
		Session session = beginTransaction();
		session.save(item);
		commit();

		firePropertyChange(DocumentController.ItemProps.NEW.toString(), null,
				item);
	}

	public synchronized void setDeleteItem(Item item) {
		if (item.getId() == null) {
			return;
		}
		// delete item assignments
		Session session = beginTransaction();
		ItemLinkDAOImpl dao = new ItemLinkDAOImpl(session);
		List<ItemLink> links = dao.findByItem(item);
		commit();
		for (ItemLink link : links) {
			setDeleteItemLink(link);
		}
		// delete item
		session = beginTransaction();
		session.delete(item);
		commit();
		firePropertyChange(DocumentController.ItemProps.DELETE.toString(),
				item, null);
	}

	public synchronized void setDeleteMultiItems(ArrayList<Long> ids) {
		for (Long id : ids) {
			Session session = beginTransaction();
			ItemDAOImpl dao = new ItemDAOImpl(session);
			Item old = dao.find(id);
			commit();
			setDeleteItem(old);
		}
	}

	// tag links
	public void setEditTagLink(TagLink editTagLink) {
		firePropertyChange(DocumentController.TagLinkProps.EDIT.toString(),
				null, editTagLink);
	}

	public synchronized void setUpdateTagLink(TagLink tagLink) {
		Session session = beginTransaction();
		TagLinkDAOImpl dao = new TagLinkDAOImpl(session);
		TagLink old = dao.find(tagLink.getId());
		commit();

		session = beginTransaction();
		session.update(tagLink);
		commit();

		firePropertyChange(DocumentController.TagLinkProps.UPDATE.toString(),
				old, tagLink);
	}

	public synchronized void setNewTagLink(TagLink tagLink) {
		Session session = beginTransaction();
		session.save(tagLink);
		commit();

		firePropertyChange(DocumentController.TagLinkProps.NEW.toString(),
				null, tagLink);
	}

	public synchronized void setDeleteTagLink(TagLink tagLink) {
		if (tagLink.getId() == null) {
			return;
		}
		Session session = beginTransaction();
		session.delete(tagLink);
		commit();
		firePropertyChange(DocumentController.TagLinkProps.DELETE.toString(),
				tagLink, null);
	}

	public synchronized void setDeleteMultiTagLinks(ArrayList<Long> ids) {
		for (Long id : ids) {
			Session session = beginTransaction();
			TagLinkDAOImpl dao = new TagLinkDAOImpl(session);
			TagLink old = dao.find(id);
			commit();
			setDeleteTagLink(old);
		}
	}

	// item links
	public void setEditItemLink(ItemLink editItemLink) {
		firePropertyChange(DocumentController.ItemLinkProps.EDIT.toString(),
				null, editItemLink);
	}

	public synchronized void setUpdateItemLink(ItemLink itemLink) {
		Session session = beginTransaction();
		ItemLinkDAOImpl dao = new ItemLinkDAOImpl(session);
		ItemLink old = dao.find(itemLink.getId());
		commit();

		session = beginTransaction();
		session.update(itemLink);
		commit();

		firePropertyChange(DocumentController.ItemLinkProps.UPDATE.toString(),
				old, itemLink);
	}

	public synchronized void setNewItemLink(ItemLink itemLink) {
		Session session = beginTransaction();
		session.save(itemLink);
		commit();

		firePropertyChange(DocumentController.ItemLinkProps.NEW.toString(),
				null, itemLink);
	}

	public synchronized void setDeleteItemLink(ItemLink itemLink) {
		if (itemLink.getId() == null) {
			return;
		}
		Session session = beginTransaction();
		session.delete(itemLink);
		commit();
		firePropertyChange(DocumentController.ItemLinkProps.DELETE.toString(),
				itemLink, null);
	}

	public synchronized void setDeleteMultiItemLinks(ArrayList<Long> ids) {
		for (Long id : ids) {
			Session session = beginTransaction();
			ItemLinkDAOImpl dao = new ItemLinkDAOImpl(session);
			ItemLink old = dao.find(id);
			commit();
			setDeleteItemLink(old);
		}
	}

	// scenes
	public void setEditScene(Scene editScene) {
		firePropertyChange(DocumentController.SceneProps.EDIT.toString(), null,
				editScene);
	}

	public synchronized void setUpdateScene(Scene scene) {
		// needed, see ChronoPanel.modelPropertyChange()
		Session session = beginTransaction();
		Scene old = (Scene) session.get(Scene.class, scene.getId());
		commit();
		try {
			session = beginTransaction();
			session.update(scene);
			commit();
		} catch (ConstraintViolationException e) {
			e.printStackTrace();
		}
		firePropertyChange(DocumentController.SceneProps.UPDATE.toString(),
				old, scene);
	}

	public synchronized void setNewScene(Scene scene) {
		Session session = beginTransaction();
		session.save(scene);
		commit();

		firePropertyChange(DocumentController.SceneProps.NEW.toString(), null,
				scene);
	}

	public synchronized void setDeleteScene(Scene scene) {
		if (scene.getId() == null) {
			return;
		}
		// delete tag / item links
		EntityUtil.deleteTagAndItemLinks(this, scene);
		// remove relative scene of affected scenes
		Session session = beginTransaction();
		SceneDAOImpl dao = new SceneDAOImpl(session);
		List<Scene> scenes = dao.findScenesWithRelativeSceneId(scene);
		commit();
		for (Scene scene2 : scenes) {
			scene2.removeRelativeScene();
			setUpdateScene(scene2);
		}
		// delete scene
		session = beginTransaction();
		session.delete(scene);
		commit();
		firePropertyChange(DocumentController.SceneProps.DELETE.toString(),
				scene, null);
	}

	public synchronized void setDeleteMultiScenes(ArrayList<Long> ids) {
		for (Long id : ids) {
			Session session = beginTransaction();
			SceneDAOImpl dao = new SceneDAOImpl(session);
			Scene old = dao.find(id);
			commit();
			setDeleteScene(old);
		}
	}

	// internals
	public void setEditInternal(Internal editInternal) {
		firePropertyChange(DocumentController.InternalProps.EDIT.toString(),
				null, editInternal);
	}

	public synchronized void setUpdateInternal(Internal internal) {
		Session session = beginTransaction();
		InternalDAOImpl dao = new InternalDAOImpl(session);
		Internal old = dao.find(internal.getId());
		commit();

		session = beginTransaction();
		session.update(internal);
		commit();

		firePropertyChange(DocumentController.InternalProps.UPDATE.toString(),
				old, internal);
	}

	public synchronized void setNewInternal(Internal internal) {
		Session session = beginTransaction();
		session.save(internal);
		commit();

		firePropertyChange(DocumentController.InternalProps.NEW.toString(),
				null, internal);
	}

	public synchronized void setDeleteInternal(Internal internal) {
		if (internal.getId() == null) {
			return;
		}
		Session session = beginTransaction();
		session.delete(internal);
		commit();
		firePropertyChange(DocumentController.InternalProps.DELETE.toString(),
				internal, null);
	}

	public synchronized void setDeleteMultiInternals(ArrayList<Long> ids) {
		for (Long id : ids) {
			Session session = beginTransaction();
			InternalDAOImpl dao = new InternalDAOImpl(session);
			Internal old = dao.find(id);
			commit();
			setDeleteInternal(old);
		}
	}
}
