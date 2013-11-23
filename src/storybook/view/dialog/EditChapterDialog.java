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

package storybook.view.dialog;

import javax.swing.JDialog;
import javax.swing.JPanel;

import storybook.model.hbn.entity.Chapter;

/**
 * @author martin
 *
 */
@SuppressWarnings("serial")
@Deprecated
public class EditChapterDialog extends JDialog {

//	private ChapterController controller;
	private JPanel panel;

	public EditChapterDialog(Chapter chapter) {
		panel = new JPanel(null);
	}

//	public EditChapterDialog(ChapterController controller) {
//		this.controller = controller;
//
//		panel = new EditChapterPanel();
//		controller.attachView(panel);
//		controller.fireAgain();
//		setModal(true);
//		Container cont = getContentPane();
//		cont.add(panel, BorderLayout.CENTER);
//		JButton ok = new JButton(new AbstractAction("OK") {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				Long id = panel.getId();
//				getThis().controller.changeChapterId(id);
//				String newTitle = getThis().panel.getText();
//				getThis().controller.changeChapterTitel(newTitle);
//				Integer newNumber = getThis().panel.getNumber();
//				getThis().controller.changeChapterNo(newNumber);
//				dispose();
//				getThis().controller.detachView(getThis().panel);
//			}
//		});
//		cont.add(ok, BorderLayout.SOUTH);
//	}

	public EditChapterDialog getThis() {
		return this;
	}
}
