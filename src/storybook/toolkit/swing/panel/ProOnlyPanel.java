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

package storybook.toolkit.swing.panel;

import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.net.URL;
import java.util.Locale;

import javax.swing.JLabel;

import storybook.SbConstants;
import storybook.toolkit.I18N;
import storybook.toolkit.swing.SwingUtil;
import storybook.view.AbstractPanel;
import storybook.view.MainFrame;
import storybook.view.net.BrowserPanel;

import net.miginfocom.swing.MigLayout;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class ProOnlyPanel extends AbstractPanel {

	public ProOnlyPanel(MainFrame mainFrame) {
		super(mainFrame);
		initAll();
	}

	@Override
	public void init() {
	}

	@Override
	public void initUi() {
		removeAll();
		setLayout(new MigLayout("wrap,fill,ins 0"));
		setPreferredSize(new Dimension(620, 420));
		setBackground(Color.white);
		setBorder(SwingUtil.getBorderDefault());
		setOpaque(true);
		try {
			String locale = Locale.getDefault().toString();
			URL url = new URL(SbConstants.URL.BANNER_URL.toString()
					+ "/?locale=" + locale);
			BrowserPanel browserPanel = new BrowserPanel(url.toString());
			browserPanel.setBackground(Color.white);
			add(browserPanel, "grow");
		} catch (Exception e) {
			add(new JLabel(I18N.getMsg("msg.pro.dlg.msg")));
		}

		revalidate();
		repaint();
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
	}
}
