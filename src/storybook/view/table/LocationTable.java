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
import storybook.model.hbn.dao.LocationDAOImpl;
import storybook.model.hbn.entity.AbstractEntity;
import storybook.model.hbn.entity.Location;
import storybook.view.MainFrame;
import storybook.view.SbColumnFactory;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class LocationTable extends AbstractTable {

	public LocationTable(MainFrame mainFrame) {
		super(mainFrame);
	}

	@Override
	public void init() {
		columns = SbColumnFactory.getInstance().getLocationColumns();
	}

	protected void modelPropertyChangeLocal(PropertyChangeEvent evt) {
		try {
			String propName = evt.getPropertyName();
			if (DocumentController.LocationProps.INIT.check(propName)) {
				initTableModel(evt);
			} else if (DocumentController.LocationProps.UPDATE.check(propName)) {
				updateEntity(evt);
			} else if (DocumentController.LocationProps.NEW.check(propName)) {
				newEntity(evt);
			} else if (DocumentController.LocationProps.DELETE.check(propName)) {
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
		Location location = (Location) getEntityFromRow(row);
		ctrl.setLocationToEdit(location);
		mainFrame.showView(ViewName.EDITOR);
	}

	@Override
	protected void sendSetNewEntityToEdit(AbstractEntity entity) {
		ctrl.setLocationToEdit((Location) entity);
		mainFrame.showView(ViewName.EDITOR);
	}

	@Override
	protected synchronized void sendDeleteEntity(int row) {
		Location location = (Location) getEntityFromRow(row);
		ctrl.deleteLocation(location);
	}

	@Override
	protected synchronized void sendDeleteEntities(int[] rows) {
		ArrayList<Long> ids = new ArrayList<Long>();
		for (int row : rows) {
			Location location = (Location) getEntityFromRow(row);
			ids.add(location.getId());
		}
		ctrl.deleteMultiLocations(ids);
	}

	@Override
	protected AbstractEntity getEntity(Long id) {
		DocumentModel model = mainFrame.getDocumentModel();
		Session session = model.beginTransaction();
		LocationDAOImpl dao = new LocationDAOImpl(session);
		Location location = dao.find(id);
		model.commit();
		return location;
	}

	@Override
	protected AbstractEntity getNewEntity() {
		return new Location();
	}
}
