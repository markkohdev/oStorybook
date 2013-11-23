/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package storybook.view.chart;
import java.beans.PropertyChangeEvent;

import javax.swing.JComponent;

import storybook.view.AbstractPanel;
import storybook.view.MainFrame;

/**
 *
 * @author favdb
 */
public class WiWWChart extends AbstractPanel {
	//protected MainFrame mainFrame;
	protected JComponent parent;

	protected boolean canceled = false;

	public WiWWChart() {
		this.mainFrame = null;
		this.parent = null;
	}

	public WiWWChart(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
		this.parent = null;
	}

	public WiWWChart(JComponent parent) {
		this.parent = parent;
		this.mainFrame = null;
	}

	@Override
	public void init() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void initUi() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void modelPropertyChange(PropertyChangeEvent evt) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
