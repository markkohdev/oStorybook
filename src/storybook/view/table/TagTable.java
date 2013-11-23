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

package storybook.view.table;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;

import org.hibernate.Session;
import storybook.SbConstants.ViewName;
import storybook.controller.DocumentController;
import storybook.model.DocumentModel;
import storybook.model.hbn.dao.TagDAOImpl;
import storybook.model.hbn.entity.AbstractEntity;
import storybook.model.hbn.entity.Tag;
import storybook.view.MainFrame;
import storybook.view.SbColumnFactory;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class TagTable extends AbstractTable {

	public TagTable(MainFrame mainFrame) {
		super(mainFrame);
	}

	@Override
	public void init() {
		columns = SbColumnFactory.getInstance().getTagColumns();
		allowMultiDelete = true;
	}

	protected void modelPropertyChangeLocal(PropertyChangeEvent evt) {
		try {
			String propName = evt.getPropertyName();
			if (DocumentController.TagProps.INIT.check(propName)) {
				initTableModel(evt);
			} else if (DocumentController.TagProps.UPDATE.check(propName)) {
				updateEntity(evt);
			} else if (DocumentController.TagProps.NEW.check(propName)) {
				newEntity(evt);
			} else if (DocumentController.TagProps.DELETE.check(propName)) {
				deleteEntity(evt);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void sendSetEntityToEdit(int row) {
		if (row == -1) {
			return;
		}
		Tag tag = (Tag) getEntityFromRow(row);
		ctrl.setTagToEdit(tag);
		mainFrame.showView(ViewName.EDITOR);
	}

	@Override
	protected void sendSetNewEntityToEdit(AbstractEntity entity) {
		ctrl.setTagToEdit((Tag) entity);
		mainFrame.showView(ViewName.EDITOR);
	}

	@Override
	protected synchronized void sendDeleteEntity(int row) {
		Tag tag = (Tag) getEntityFromRow(row);
		ctrl.deleteTag(tag);
	}

	@Override
	protected synchronized void sendDeleteEntities(int[] rows) {
		ArrayList<Long> ids = new ArrayList<Long>();
		for (int row : rows) {
			Tag tag = (Tag) getEntityFromRow(row);
			ids.add(tag.getId());
		}
		ctrl.deleteMultiTags(ids);
	}

	@Override
	protected AbstractEntity getEntity(Long id) {
		DocumentModel model = mainFrame.getDocumentModel();
		Session session = model.beginTransaction();
		TagDAOImpl dao = new TagDAOImpl(session);
		Tag tag = dao.find(id);
		model.commit();
		return tag;
	}

	@Override
	protected AbstractEntity getNewEntity() {
		return new Tag();
	}
}
