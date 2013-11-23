/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package storybook.view.memoria;

import java.beans.PropertyChangeEvent;

import javax.swing.JComponent;

import storybook.view.AbstractPanel;
import storybook.view.MainFrame;

/**
 *
 * @author favdb
 */
public class MemoriaPanel extends AbstractPanel {
	//protected MainFrame mainFrame;
	protected JComponent parent;

	protected boolean canceled = false;

	public MemoriaPanel() {
		this.mainFrame = null;
		this.parent = null;
	}

	public MemoriaPanel(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
		this.parent = null;
	}

	public MemoriaPanel(JComponent parent) {
		this.parent = parent;
		this.mainFrame = null;
	}

	@Override
	public void init() {
	}

	@Override
	public void initUi() {
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
	}

}
