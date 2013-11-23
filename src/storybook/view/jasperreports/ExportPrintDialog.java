/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package storybook.view.jasperreports;

import javax.swing.JComponent;
import javax.swing.JDialog;

import storybook.view.MainFrame;

/**
 *
 * @author favdb
 */
public class ExportPrintDialog extends JDialog {
	protected MainFrame mainFrame;
	protected JComponent parent;

	protected boolean canceled = false;

	public ExportPrintDialog() {
		this.mainFrame = null;
		this.parent = null;
	}

	public ExportPrintDialog(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
		this.parent = null;
	}

	public ExportPrintDialog(JComponent parent) {
		this.parent = parent;
		this.mainFrame = null;
	}

}
