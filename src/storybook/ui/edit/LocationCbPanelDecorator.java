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

package storybook.ui.edit;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import org.apache.commons.lang3.text.WordUtils;
import storybook.model.hbn.entity.AbstractEntity;
import storybook.model.hbn.entity.Location;

/**
 * @author martin
 *
 */
public class LocationCbPanelDecorator extends CbPanelDecorator {
	private String oldCity = "";
	private String oldCountry = "";

	public LocationCbPanelDecorator() {
	}

	@Override
	public void decorateBeforeEntity(AbstractEntity entity) {
		Location p = (Location) entity;
		String country = WordUtils.capitalize(p.getCountry());
		String city = WordUtils.capitalize(p.getCity());
		if (!oldCountry.equals(country) || !oldCity.equals(city)) {
			StringBuilder buf = new StringBuilder();
			buf.append("<html>");
			buf.append("<p style='margin-top:5px'>");
			buf.append("<b>").append(city).append("</b>");
			if (!country.isEmpty()) {
				if (!city.isEmpty()) {
					buf.append(" (");
				} else {
					buf.append("<b>");
				}
				buf.append(country);
				if (!city.isEmpty()) {
					buf.append(")");
				} else {
					buf.append("</b>");
				}
			}
			JLabel lb = new JLabel(buf.toString());
			// lb.setFont(FontUtil.getBoldFont());
			panel.add(lb, "span");
			oldCountry = country;
			oldCity = city;
		}
	}

	@Override
	public void decorateEntity(JCheckBox cb, AbstractEntity entity) {
		if (!oldCountry.isEmpty() || !oldCity.isEmpty()) {
			panel.add(new JLabel("<html><p style='margin-left:5px'>&nbsp;"), "split 2");
		}
		panel.add(cb);
	}

	@Override
	public void decorateAfterEntity(AbstractEntity entity) {
	}
}
