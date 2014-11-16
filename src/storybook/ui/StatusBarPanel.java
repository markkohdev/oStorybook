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
import storybook.SbApp;
import storybook.SbConstants.PreferenceKey;
import storybook.action.LoadDockingLayoutAction;
import storybook.controller.BookController;
import storybook.model.BookModel;
import storybook.model.PreferenceModel;
import storybook.model.hbn.dao.ChapterDAOImpl;
import storybook.model.hbn.dao.PartDAOImpl;
import storybook.model.hbn.dao.PreferenceDAOImpl;
import storybook.model.hbn.dao.SceneDAOImpl;
import storybook.model.hbn.entity.Chapter;
import storybook.model.hbn.entity.Preference;
import storybook.model.hbn.entity.Scene;
import storybook.toolkit.I18N;
import storybook.toolkit.swing.panel.MemoryMonitorPanel;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
public class StatusBarPanel extends AbstractPanel implements ActionListener {

	private JLabel lbParts;
	private JLabel lbStat;
	private int nbWords;
	private int nbCharacters;
	private int nbChapters;
	private int nbScenes;

	//JComboBox layoutCombo = new JComboBox();

	public StatusBarPanel(MainFrame mainFrame) {
		SbApp.trace("StatusBarPanel(mainFrame)");
		this.mainFrame = mainFrame;
		initAll();
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		SbApp.trace("StatusBarPanel.modelPropertyChange("+evt.toString()+")");
		String propName = evt.getPropertyName();
		// Object newValue = evt.getNewValue();
		// Object oldValue = evt.getOldValue();

		if (BookController.PartProps.CHANGE.check(propName) || BookController.PartProps.UPDATE.check(propName)) {
			refresh();
			return;
		}

		if (BookController.CommonProps.REFRESH.check(propName)) {
			refresh();
			return;
		}
		
		if (BookController.ChapterProps.INIT.check(propName)
				|| BookController.ChapterProps.DELETE.check(propName)
				|| BookController.ChapterProps.DELETE_MULTI.check(propName)
				|| BookController.ChapterProps.NEW.check(propName)
				|| BookController.ChapterProps.UPDATE.check(propName)
				|| BookController.SceneProps.INIT.check(propName)
				|| BookController.SceneProps.DELETE.check(propName)
				|| BookController.SceneProps.DELETE_MULTI.check(propName)
				|| BookController.SceneProps.NEW.check(propName)
				|| BookController.SceneProps.UPDATE.check(propName)) {
			refreshStat();
//			return;
		}

	}

	@Override
	public void init() {
		SbApp.trace("StatusBarPanel.init()");
		computeStatistics();
	}

	@Override
	public void initUi() {
		SbApp.trace("StatusBarPanel.initUi()");
		setLayout(new MigLayout("flowx,fill,ins 1", "[][grow][][]"));

		// add(new DebugInfoPanel(mainFrame));

		lbParts = new JLabel(" " + I18N.getMsgColon("msg.common.current.part")
				+ " " + mainFrame.getCurrentPart().toString());
		add(lbParts);
/*
		layoutCombo = new JComboBox();
		layoutCombo.setPreferredSize(new Dimension(200, 20));
		// layoutCombo.setFont(new Font("Dialog", Font.PLAIN, 10));
		layoutCombo.setMaximumSize(new Dimension(200, 20));
		refreshLayoutCombo();
		layoutCombo.addActionListener(this);
		add(layoutCombo, "al right");
*/
		String strStat="Statistics:";
		strStat+=" "+I18N.getMsg("msg.common.chapters")+"="+nbChapters;
		strStat+=" "+I18N.getMsg("msg.common.scenes")+"="+nbScenes;
		strStat+=" "+I18N.getMsg("msg.common.characters")+"="+nbCharacters;
		strStat+=" "+I18N.getMsg("msg.common.words")+"="+nbWords;
		lbStat=new JLabel(strStat);
		add(lbStat, "al center");
		MemoryMonitorPanel memPanel = new MemoryMonitorPanel();
		add(memPanel, "al right");

		revalidate();
		repaint();
	}

	@SuppressWarnings("unchecked")
	private void refreshLayoutCombo() {
		/*
		layoutCombo.removeAllItems();
		layoutCombo.addItem("");
		PreferenceModel model = SbApp.getInstance().getPreferenceModel();
		Session session = model.beginTransaction();
		PreferenceDAOImpl dao = new PreferenceDAOImpl(session);
		List<Preference> pref = dao.findAll();
		for (Preference preference : pref) {
			if (preference.getKey().startsWith(PreferenceKey.DOCKING_LAYOUT.toString())) {
				String name = preference.getStringValue();
				if (SbConstants.BookKey.LAST_USED_LAYOUT.toString().equals(name)) {
					continue;
				}
				LoadDockingLayoutAction act = new LoadDockingLayoutAction(mainFrame, name);
				layoutCombo.addItem(act);
			}
		}
		model.commit();
				*/
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SbApp.trace("StatusBarPanel.actionPerformed("+e.paramString()+")");
/*
		Object o = layoutCombo.getSelectedItem();
		if (o instanceof LoadDockingLayoutAction) {
			LoadDockingLayoutAction act = (LoadDockingLayoutAction) o;
			act.actionPerformed(null);
		}
		*/
		String strStat="Statistics:";
		strStat+=" "+I18N.getMsg("msg.common.chapters")+"="+nbChapters;
		strStat+=" "+I18N.getMsg("msg.common.scenes")+"="+nbScenes;
		strStat+=" "+I18N.getMsg("msg.common.characters")+"="+nbCharacters;
		strStat+=" "+I18N.getMsg("msg.common.words")+"="+nbWords;
		lbStat.setText(strStat);
	}
	
	private void computeStatistics() {
		SbApp.trace("StatusBarPanel.computeStatistics()");
		nbWords=0;
		nbCharacters=0;
		nbChapters=0;
		nbScenes=0;
		BookModel model = mainFrame.getBookModel();
		Session session = model.beginTransaction();
		ChapterDAOImpl ChapterDAO = new ChapterDAOImpl(session);
		List<Chapter> chapters = ChapterDAO.findAll();
		nbChapters=chapters.size();
		SceneDAOImpl SceneDAO = new SceneDAOImpl(session);
		List<Scene> scenes = SceneDAO.findAll();
		nbScenes=scenes.size();
		for (Scene scene : scenes) {
			nbCharacters+=scene.numberOfCharacters();
			nbWords+=scene.numberOfWords();
		}
	}

	private void refreshStat() {
		SbApp.trace("StatusBarPanel.refreshStat()");
		computeStatistics();
		String strStat="Statistics:";
		strStat+=" "+I18N.getMsg("msg.common.chapters")+"="+nbChapters;
		strStat+=" "+I18N.getMsg("msg.common.scenes")+"="+nbScenes;
		strStat+=" "+I18N.getMsg("msg.common.characters")+"="+nbCharacters;
		strStat+=" "+I18N.getMsg("msg.common.words")+"="+nbWords;
		lbStat.setText(strStat);
		revalidate();
		repaint();
		SbApp.trace(strStat);
	}
}
