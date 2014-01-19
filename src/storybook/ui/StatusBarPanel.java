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

package storybook.ui;

import storybook.ui.panel.AbstractPanel;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.hibernate.Session;
import storybook.SbConstants;
import storybook.StorybookApp;
import storybook.SbConstants.PreferenceKey;
import storybook.action.LoadDockingLayoutAction;
import storybook.controller.DocumentController;
import storybook.model.PreferenceModel;
import storybook.model.hbn.dao.PreferenceDAOImpl;
import storybook.model.hbn.entity.Preference;
import storybook.toolkit.I18N;
import storybook.toolkit.swing.panel.MemoryMonitorPanel;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class StatusBarPanel extends AbstractPanel implements ActionListener {

	private JLabel lbParts;

	JComboBox layoutCombo = new JComboBox();

	public StatusBarPanel(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
		initAll();
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		String propName = evt.getPropertyName();
		// Object newValue = evt.getNewValue();
		// Object oldValue = evt.getOldValue();

		if (DocumentController.PartProps.CHANGE.check(propName)
				|| DocumentController.PartProps.UPDATE.check(propName)) {
			refresh();
			return;
		}

		if (DocumentController.CommonProps.REFRESH.check(propName)) {
			refresh();
//			return;
		}
	}

	@Override
	public void init() {
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout("flowx,fill,ins 1", "[][grow][][]"));

		// add(new DebugInfoPanel(mainFrame));

		lbParts = new JLabel(" " + I18N.getMsgColon("msg.common.current.part")
				+ " " + mainFrame.getCurrentPart().toString());
		add(lbParts);

		layoutCombo = new JComboBox();
		layoutCombo.setPreferredSize(new Dimension(200, 20));
		// layoutCombo.setFont(new Font("Dialog", Font.PLAIN, 10));
		layoutCombo.setMaximumSize(new Dimension(200, 20));
		refreshLayoutCombo();
		layoutCombo.addActionListener(this);
		add(layoutCombo, "al right");

		MemoryMonitorPanel memPanel = new MemoryMonitorPanel();
		add(memPanel, "al right");

		revalidate();
		repaint();
	}

	private void refreshLayoutCombo() {
		layoutCombo.removeAllItems();
		layoutCombo.addItem("");
		PreferenceModel model = StorybookApp.getInstance().getPreferenceModel();
		Session session = model.beginTransaction();
		PreferenceDAOImpl dao = new PreferenceDAOImpl(session);
		List<Preference> pref = dao.findAll();
		for (Preference preference : pref) {
			if (preference.getKey().startsWith(
					PreferenceKey.DOCKING_LAYOUT.toString())) {
				String name = preference.getStringValue();
				if (SbConstants.InternalKey.LAST_USED_LAYOUT.toString().equals(
						name)) {
					continue;
				}
				LoadDockingLayoutAction act = new LoadDockingLayoutAction(
						mainFrame, name);
				layoutCombo.addItem(act);
			}
		}
		model.commit();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = layoutCombo.getSelectedItem();
		if (o instanceof LoadDockingLayoutAction) {
			LoadDockingLayoutAction act = (LoadDockingLayoutAction) o;
			act.actionPerformed(null);
		}
	}
}
