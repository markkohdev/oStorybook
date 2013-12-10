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

import java.awt.Dimension;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import storybook.model.EntityUtil;
import storybook.model.hbn.entity.AbstractEntity;
import storybook.toolkit.I18N;
import storybook.toolkit.html.HtmlUtil;
import storybook.toolkit.swing.SwingUtil;
import storybook.view.MainFrame;

import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class ConfirmMultiDeletionDlg extends AbstractDialog {

	private List<AbstractEntity> entities;

	public ConfirmMultiDeletionDlg(MainFrame mainFrame,
			List<AbstractEntity> entities) {
		super(mainFrame);
		this.entities = entities;
		initAll();
	}

	@Override
	public void init() {
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout("wrap,fill"));
		setPreferredSize(new Dimension(400, 350));
		setTitle(I18N.getMsg("msg.common.delete"));
		setIconImage(I18N.getIconImage("icon.small.delete"));

		JLabel lbQuestion = new JLabel(
				I18N.getMsg("msg.common.multi.delete.question"));

		StringBuilder buf = new StringBuilder();
		buf.append(HtmlUtil.getHeadWithCSS());
		for (AbstractEntity entity : entities) {
			buf.append("<p style='margin-bottom:10px'>\n");
			buf.append(EntityUtil.getEntityFullTitle(entity));
			buf.append("</p>\n");
			buf.append("<p style=''>\n");
			buf.append(EntityUtil.getDeleteInfo(mainFrame, entity));
			buf.append("</p>\n");
			buf.append("<hr style='margin:10px'>\n");
		}
		JTextPane tpEntity = new JTextPane();
		tpEntity.setContentType("text/html");
		tpEntity.setEditable(false);
		tpEntity.setText(buf.toString());
		tpEntity.setCaretPosition(0);
		JScrollPane scroller = new JScrollPane(tpEntity);
		SwingUtil.setMaxPreferredSize(scroller);

		add(lbQuestion, "gap 0 0 10 10");
		add(scroller, "grow");
		add(getOkButton(), "sg,split 2,right");
		add(getCancelButton(), "sg");
	}

	public List<AbstractEntity> getEntities() {
		return entities;
	}
}
