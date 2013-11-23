/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package storybook.view.chart;

/**
 *
 * @author favdb
 */
import java.beans.PropertyChangeEvent;

import javax.swing.JComponent;

import storybook.view.AbstractPanel;
import storybook.view.MainFrame;

/**
 *
 * @author favdb
 */
public class GanttChart extends AbstractPanel {
	//protected MainFrame mainFrame;
	protected JComponent parent;

	protected boolean canceled = false;

	public GanttChart() {
		this.mainFrame = null;
		this.parent = null;
	}

	public GanttChart(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
		this.parent = null;
	}

	public GanttChart(JComponent parent) {
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
