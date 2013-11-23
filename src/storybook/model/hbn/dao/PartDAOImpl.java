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

package storybook.model.hbn.dao;

import java.util.List;
import java.util.Vector;

import javax.persistence.NonUniqueResultException;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import storybook.model.hbn.entity.AbstractEntity;
import storybook.model.hbn.entity.Chapter;
import storybook.model.hbn.entity.Part;

public class PartDAOImpl extends SbGenericDAOImpl<Part, Long> implements
		PartDAO {

	public PartDAOImpl() {
		super();
	}

	public PartDAOImpl(Session session) {
		super(session);
	}

	public Part findFirst() {
		List<Part> ret = findAll();
		return ret.get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Part> findAll() {
		Query query = session.createQuery("from Part order by number");
		List<Part> ret = (List<Part>) query.list();
		return ret;
	}

	@SuppressWarnings("unchecked")
	public List<Chapter> findChapters(Part part) {
		Criteria crit = session.createCriteria(Chapter.class);
		crit.add(Restrictions.eq("part", part));
		crit.addOrder(Order.asc("chapterno"));
		List<Chapter> chapters = (List<Chapter>) crit.list();
		return chapters;
	}

	public int getNextPartNumber() {
		return getMaxPartNumber() + 1;
	}

	public int getMaxPartNumber() {
		Query query = session.createQuery("select max(number) from Part");
		Integer ret = (Integer) query.uniqueResult();
		return ret;
	}

	@SuppressWarnings("unchecked")
	public boolean checkIfNumberExists(AbstractEntity entity) {
		try {
			Part newPart = (Part) entity;
			Integer newNumber = newPart.getNumber();

			if (newPart.getId() != -1) {
				// update
				PartDAOImpl dao = new PartDAOImpl(session);
				Part oldPart = dao.find(newPart.getId());
				Integer oldNumber = oldPart.getNumber();
				Criteria crit = session.createCriteria(Part.class);
				crit.add(Restrictions.eq("number", newNumber));
				List<Part> parts = (List<Part>) crit.list();
				Vector<Integer> numbers = new Vector<Integer>();
				for (Part part : parts) {
					numbers.add(part.getNumber());
				}
				if (newNumber == oldNumber) {
					numbers.remove(newNumber);
				}
				if (numbers.size() > 0) {
					return false;
				}
				return true;
			}

			// new
			Criteria crit = session.createCriteria(Part.class);
			crit.add(Restrictions.eq("number", newNumber));
			List<Part> parts = (List<Part>) crit.list();
			if (parts.size() > 0) {
				return false;
			}

			return true;
		} catch (NonUniqueResultException e) {
			e.printStackTrace();
			return true;
		}
	}
}
