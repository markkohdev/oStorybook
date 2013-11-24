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

/* SB5 suppress Pro
import java.awt.Color;

import javax.swing.JPanel;

import storybook.toolkit.I18N;
import storybook.toolkit.swing.panel.ProOnlyPanel;
import storybook.view.MainFrame;

import net.miginfocom.swing.MigLayout;
*/
/**
 * @author martin
 *
 */
/* SB5 suppress Pro
@SuppressWarnings("serial")
public class ProOnlyDialog extends AbstractDialog {

	public ProOnlyDialog(MainFrame mainFrame) {
		super(mainFrame);
		initAll();
	}

	@Override
	public void init() {
	}

	@Override
	public void initUi() {
		setLayout(new MigLayout("wrap,fill,ins 10"));
		setTitle(I18N.getMsg("msg.pro.dlg.title"));
		setBackground(Color.white);

		JPanel panel = new ProOnlyPanel(mainFrame);

		// layout
		add(panel, "grow");
		add(getCloseButton(), "right,sg");
	}
}
*/