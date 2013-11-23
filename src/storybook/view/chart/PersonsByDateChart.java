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
public class PersonsByDateChart extends AbstractPanel {
	//protected MainFrame mainFrame;
	protected JComponent parent;

	protected boolean canceled = false;

	public PersonsByDateChart() {
		this.mainFrame = null;
		this.parent = null;
	}

	public PersonsByDateChart(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
		this.parent = null;
	}

	public PersonsByDateChart(JComponent parent) {
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
